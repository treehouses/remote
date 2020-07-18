package io.treehouses.remote.Fragments.DialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.treehouses.remote.bases.FullScreenDialogFragment
import io.treehouses.remote.databinding.KeysDialogBinding
import java.security.InvalidAlgorithmParameterException
import java.security.KeyPairGenerator

class SSHKeyGen : FullScreenDialogFragment() {
    private lateinit var bind : KeysDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = KeysDialogBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.cancelButton.setOnClickListener { dismiss() }

        bind.generateKey.setOnClickListener {
            generateKey(name = bind.keyNameInput.text.toString(), keyType = bind.keyTypeSpinner.selectedItem.toString(), password = bind.passwordInput.text.toString())
        }
    }

    private fun generateKey(name: String, keyType: String, password: String) {
        val key = if (keyType == "RSA") {
            generateRSA()
        } else {
            throw InvalidAlgorithmParameterException()
        }
    }

    private fun generateRSA() {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(512)
        val keypair = keyGen.genKeyPair()


    }

}