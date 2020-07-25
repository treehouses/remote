package io.treehouses.remote.Fragments.DialogFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import io.treehouses.remote.R
import io.treehouses.remote.SSH.PubKeyUtils
import io.treehouses.remote.SSH.beans.PubKeyBean
import io.treehouses.remote.bases.FullScreenDialogFragment
import io.treehouses.remote.databinding.KeysDialogBinding
import io.treehouses.remote.utils.KeyUtils
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.*

class SSHKeyGen : FullScreenDialogFragment() {
    private lateinit var bind: KeysDialogBinding
    private lateinit var allKeys: MutableList<String>
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

        bind.generateKey.setOnClickListener {
            val name = bind.keyNameInput.text.toString()
            if (validate(name)) {
                generateKey(
                        name = name,
                        algorithm = bind.keyTypeSpinner.selectedItem.toString(),
                        password = bind.passwordInput.text.toString()
                )
            }
        }
    }

    private fun addTextChangeListener() {
        bind.keyNameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (validate(s.toString())) bind.keyNameLayout.error = null
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
        return if (errorMsg.isEmpty()) true
        else {
            bind.keyNameLayout.error = errorMsg
            false
        }
    }

    private fun generateKey(name: String, algorithm: String, password: String) {
        val keyPair = generateKeyPair(algorithm)
        val key = PubKeyBean(name, algorithm, PubKeyUtils.getEncodedPrivate(keyPair.private, password), keyPair.public.encoded)
        if (password.isNotEmpty()) key.isEncrypted = true
        Log.e("PUBLIC ENCODED: ", keyPair.public.encoded.toString())
        Log.e("PUBLIC ENCODED: ", keyPair.private.encoded.toString())
        KeyUtils.saveKey(requireContext(), key)
        dismiss()
    }

    private fun generateKeyPair(algorithm: String): KeyPair {
        val keyGen = KeyPairGenerator.getInstance(algorithm)
        keyGen.initialize(if (algorithm == "EC") 256 else if (algorithm == "RSA") 2048 else 1024)
        val keyPair = keyGen.generateKeyPair()

        Log.e("GENERATED Public", Arrays.toString(keyPair.public.encoded))
        Log.e("GENERATED Private", keyPair.private.toString())
        return keyPair
    }

}