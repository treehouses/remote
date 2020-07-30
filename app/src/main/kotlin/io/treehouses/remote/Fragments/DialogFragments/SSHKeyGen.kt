package io.treehouses.remote.Fragments.DialogFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import io.treehouses.remote.R
import io.treehouses.remote.SSH.PubKeyUtils
import io.treehouses.remote.SSH.beans.PubKeyBean
import io.treehouses.remote.bases.FullScreenDialogFragment
import io.treehouses.remote.databinding.KeysDialogBinding
import io.treehouses.remote.utils.KeyUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.KeyPairGenerator

class SSHKeyGen : FullScreenDialogFragment() {

    companion object {
        val VALID_EC_SIZES = listOf(256, 384, 521)
    }

    private lateinit var bind: KeysDialogBinding
    private lateinit var allKeys: MutableList<String>
    private var max = 1024
    private var min = 1024
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = KeysDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        allKeys = KeyUtils.getAllKeyNames(requireContext())
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.cancelButton.setOnClickListener { dismiss() }

        bind.keyTypeSpinner.adapter = ArrayAdapter<String>(
                requireContext(),
                R.layout.key_type_spinner_item,
                R.id.itemTitle,
                resources.getStringArray(R.array.key_types))

        addTextChangeListener()

        bind.generateKey.isEnabled = false
        bind.generateKey.setOnClickListener {
            val name = bind.keyNameInput.text.toString()
            if (validate(name)) {
                generateKey(
                        name = name,
                        algorithm = bind.keyTypeSpinner.selectedItem.toString(),
                        password = bind.passwordInput.text.toString(),
                        bitSize = getBitSize()
                )
            }
        }
        setUpSeekBar(getSelectedAlgo())
        setUpStrengthListeners()
        setUpShowStrength()
    }

    private fun getSelectedAlgo(): String {
        return bind.keyTypeSpinner.selectedItem as String
    }

    private fun setUpSeekBar(algorithm: String) {
        when (algorithm) {
            "RSA" -> {
                min = 1024
                max = 16384
            }
            "EC" -> {
                min = 256
                max = 521
            }
            "DSA" -> {
                min = 1024
                max = 1024
            }
        }
        bind.keyStrength.max = max - min
        if (algorithm == "RSA") setStrength(2048) else setStrength(min)
    }

    private fun setUpStrengthListeners() {
        bind.keyStrength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) bind.strengthShow.setText(getBitSize().toString())
                changeECtoClosest(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        bind.keyTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setUpSeekBar(parent?.getItemAtPosition(position) as String)
            }
        }


    }

    private fun setUpShowStrength() {
        bind.strengthShow.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val isValid = validate(bind.keyNameInput.text.toString())
                bind.generateKey.isEnabled = isValid
                val strength = if (s.isNullOrEmpty()) 0 else Integer.parseInt(s.toString())
                if (validateBitSize(strength, getSelectedAlgo())) {
                    setStrength(strength, false)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

        bind.strengthShow.setOnFocusChangeListener { _, _ ->
            val value = Integer.parseInt(bind.strengthShow.text.toString())
            when {
                value < min -> setStrength(min)
                value > max -> setStrength(max)
                else -> setStrength(value)
            }
        }
    }

    private fun addTextChangeListener() {
        bind.keyNameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (validate(s.toString())) {
                    bind.keyNameLayout.error = null
                    bind.generateKey.isEnabled = true
                } else bind.generateKey.isEnabled = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validate(keyName: String): Boolean {
        val errorMsg = when {
            keyName.isEmpty() -> "Please enter a key nickname"
            allKeys.contains(keyName) -> "Name already exists!"
            else -> ""
        }
        if (errorMsg.isNotEmpty()) {
            bind.keyNameLayout.error = errorMsg
            return false
        }
        val intToParse = bind.strengthShow.text.toString()
        if (intToParse.isEmpty()) return false
        return validateBitSize(Integer.parseInt(intToParse), getSelectedAlgo())
    }

    private fun generateKey(name: String, algorithm: String, password: String, bitSize: Int) {
        bind.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.Default) {
            val keyPair = KeyPairGenerator.getInstance(algorithm).apply {
                initialize(bitSize)
            }.generateKeyPair()
            Log.e("GENERATED", "GENERATED KEY")
            val key = PubKeyBean(name, algorithm, PubKeyUtils.getEncodedPrivate(keyPair.private, password), keyPair.public.encoded)
            if (password.isNotEmpty()) key.isEncrypted = true
            if (isActive) KeyUtils.saveKey(requireContext(), key)
            withContext(Dispatchers.Main) {
                bind.progressBar.visibility = View.GONE
                dismiss()
            }
        }
    }

    private fun setStrength(strength: Int, updateText: Boolean = true) {
        if (updateText) bind.strengthShow.setText(strength.toString())
        bind.keyStrength.progress = strength - min
    }

    private fun changeECtoClosest(progress: Int) {
        if (getSelectedAlgo() == "EC") {
            val newProgress = when {
                progress <= (max - min) / 3 -> 256
                progress >= 2 * (max - min) / 3 -> 521
                else -> 384
            }
            setStrength(newProgress)
        }
    }

    private fun validateBitSize(size: Int, algorithm: String): Boolean {
        var isValid = true
        if (size !in min..max) isValid = false
        else if (algorithm == "EC" && size !in VALID_EC_SIZES) isValid = false
        else if (algorithm == "DSA" && size != 1024) isValid = false
        return isValid
    }

    private fun getBitSize(): Int {
        return bind.keyStrength.progress + min
    }

}