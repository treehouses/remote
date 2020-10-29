package io.treehouses.remote.bases

import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import io.treehouses.remote.ssh.PubKeyUtils
import io.treehouses.remote.ssh.beans.PubKeyBean
import io.treehouses.remote.databinding.KeysDialogBinding
import io.treehouses.remote.utils.KeyUtils
import kotlinx.coroutines.*
import java.security.KeyPairGenerator
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

open class BaseSSHKeyGen : FullScreenDialogFragment() {

    companion object {
        val VALID_EC_SIZES = listOf(256, 384, 521)
    }

    protected lateinit var bind: KeysDialogBinding
    protected lateinit var allKeys: MutableList<String>
    protected var max = 1024
    protected var min = 1024

    protected fun generateKeyOnClick() {
        val name = bind.keyNameInput.text.toString()
        if (!validate(name)) return
        if (bind.inBackground.isChecked) {
            startKeyGenInBackground(
                    name,
                    bind.keyTypeSpinner.selectedItem as String,
                    bind.passwordInput.text.toString(),
                    getBitSize())
        } else {
            startKeyGen(
                    name = name,
                    algorithm = bind.keyTypeSpinner.selectedItem.toString(),
                    password = bind.passwordInput.text.toString(),
                    bitSize = getBitSize()
            )
        }
    }

    protected fun getSelectedAlgo(): String {
        return bind.keyTypeSpinner.selectedItem as String
    }

    protected fun addTextChangeListener() {
        bind.keyNameInput.addTextChangedListener {
            if (validate(it.toString())) {
                bind.keyNameLayout.error = null
                bind.generateKey.isEnabled = true
            } else bind.generateKey.isEnabled = false
        }
    }

    protected fun validate(keyName: String): Boolean {
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

    private fun startKeyGen(name: String, algorithm: String, password: String, bitSize: Int) {
        bind.progressBar.visibility = View.VISIBLE
        bind.generateKey.isEnabled = false
        bind.keyNameInput.isEnabled = false
        bind.keyTypeSpinner.isEnabled = false
        bind.keyStrength.isEnabled = false
        bind.strengthShow.isEnabled = false
        bind.inBackground.isEnabled = false
        bind.passwordInput.isEnabled = false

        lifecycleScope.launch(Dispatchers.Default) {
            val key = generateKey(name, algorithm, password, bitSize)
            if (isActive) {
                withContext(Dispatchers.Main) {
                    KeyUtils.saveKey(requireContext(), key)
                    bind.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Key Generation Complete: $name $algorithm-$bitSize-bit", Toast.LENGTH_LONG).show()
                    dismiss()
                }
            }
        }
    }

    private fun startKeyGenInBackground(name: String, algorithm: String, password: String, bitSize: Int) {
        requireActivity().run {
            GlobalScope.launch(Dispatchers.Default) {
                val key = generateKey(name, algorithm, password, bitSize)
                if (isActive) KeyUtils.saveKey(applicationContext, key)
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Key Generation Complete: $name $algorithm-$bitSize-bit", Toast.LENGTH_LONG).show()
                }
            }
        }
        dismiss()
    }

    private suspend fun generateKey(name: String, algorithm: String, password: String, bitSize: Int): PubKeyBean {
        return suspendCoroutine {
            val keyPair = KeyPairGenerator.getInstance(algorithm).apply {
                initialize(bitSize)
            }.generateKeyPair()
            val key = PubKeyBean(name, algorithm, PubKeyUtils.getEncodedPrivate(keyPair.private, password), keyPair.public.encoded)
            if (password.isNotEmpty()) key.isEncrypted = true
            it.resume(key)
        }
    }


    protected fun setStrength(strength: Int, updateText: Boolean = true) {
        if (updateText) bind.strengthShow.setText(strength.toString())
        bind.keyStrength.progress = strength - min
    }

    protected fun changeECtoClosest(progress: Int) {
        if (getSelectedAlgo() == "EC") {
            val newProgress = when {
                progress <= (max - min) / 3 -> 256
                progress >= 2 * (max - min) / 3 -> 521
                else -> 384
            }
            setStrength(newProgress)
        }
    }

    protected fun validateBitSize(size: Int, algorithm: String): Boolean {
        var isValid = true
        if (size !in min..max) isValid = false
        else if (algorithm == "EC" && size !in VALID_EC_SIZES) isValid = false
        else if (algorithm == "DSA" && size != 1024) isValid = false
        return isValid
    }

    protected fun getBitSize(): Int {
        return bind.keyStrength.progress + min
    }

}