package io.treehouses.remote.Fragments.DialogFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.treehouses.remote.SSH.PubKeyUtils
import io.treehouses.remote.SSH.beans.PubKeyBean
import io.treehouses.remote.bases.FullScreenDialogFragment
import io.treehouses.remote.databinding.KeysDialogBinding
import io.treehouses.remote.utils.KeyUtils
import io.treehouses.remote.utils.SaveUtils
import net.i2p.crypto.eddsa.Utils
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.util.*

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
            generateKey(name = bind.keyNameInput.text.toString(), algorithm = bind.keyTypeSpinner.selectedItem.toString(), password = bind.passwordInput.text.toString())
        }
    }

    private fun generateKey(name: String, algorithm: String, password: String) {
        val keyPair = generateKeyPair(algorithm)
        val key = PubKeyBean(name, algorithm, PubKeyUtils.getEncodedPrivate(keyPair.private, password), keyPair.public.encoded)
        if (!password.isEmpty()) key.isEncrypted = true
        Log.e("PUBLIC ENCODED: ", keyPair.public.encoded.toString())
        Log.e("PUBLIC ENCODED: ", keyPair.private.encoded.toString())
        KeyUtils.saveKey(requireContext(), key)
        dismiss()
    }

    private fun generateKeyPair(algorithm: String) : KeyPair {
        val keyGen = KeyPairGenerator.getInstance(algorithm)
        keyGen.initialize(if (algorithm == "EC") 256 else if (algorithm == "RSA") 2048 else 1024)
        val keyPair = keyGen.generateKeyPair()

        Log.e("GENERATED Public", Arrays.toString(keyPair.public.encoded))
        Log.e("GENERATED Private", keyPair.private.toString())
        return keyPair
    }

}