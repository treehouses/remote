package io.treehouses.remote.fragments.dialogFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseSSHKeyGen
import io.treehouses.remote.databinding.KeysDialogBinding
import io.treehouses.remote.utils.KeyUtils

class SSHKeyGen: BaseSSHKeyGen() {
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
            generateKeyOnClick()
        }
        setUpSeekBar(getSelectedAlgo())
        setUpStrengthListeners()
        setUpShowStrength()
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
}