package io.treehouses.remote.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import io.treehouses.remote.Constants
import io.treehouses.remote.fragments.dialogfragments.SSHAllKeyFragment
import io.treehouses.remote.fragments.dialogfragments.SSHKeyGenFragment
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.ssh.beans.HostBean
import io.treehouses.remote.sshconsole.SSHConsole
import io.treehouses.remote.bases.BaseSSHConfig
import io.treehouses.remote.databinding.DialogSshBinding
import io.treehouses.remote.utils.KeyUtils
import io.treehouses.remote.utils.KeyUtils.getOpenSSH
import io.treehouses.remote.utils.SaveUtils
import io.treehouses.remote.utils.Utils.toast
import io.treehouses.remote.utils.logD

class SSHConfigFragment : BaseSSHConfig() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = DialogSshBinding.inflate(inflater, container, false)
        if (listener.getChatService().state == Constants.STATE_CONNECTED) {
            listener.sendMessage(getString(R.string.TREEHOUSES_NETWORKMODE_INFO))
            listener.getChatService().updateHandler(mHandler)
        }
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEnabled(false)
        addTextValidation()
        Tutorials.sshTutorial(bind, requireActivity())
        bind.connectSsh.setOnClickListener {
            val uriString = bind.sshTextInput.text.toString()
            connect(uriString, false)
        }
        setUpAdapter()
        bind.generateKeys.setOnClickListener { SSHKeyGenFragment().show(childFragmentManager, "GenerateKey") }
        bind.smartConnect.setOnClickListener {
            val shouldConnect = checkForSmartConnectKey()
            val uriString = bind.sshTextInput.text.toString()
            if (shouldConnect) connect(uriString, true)
        }
        bind.showKeys.setOnClickListener { SSHAllKeyFragment().show(childFragmentManager, "AllKeys") }
    }

    private fun checkForSmartConnectKey(): Boolean {
        if (!KeyUtils.getAllKeyNames(requireContext()).contains("SmartConnectKey")) {
            if (listener.getChatService().state == Constants.STATE_CONNECTED) {
                val key = KeyUtils.createSmartConnectKey(requireContext())
                listener.sendMessage(getString(R.string.TREEHOUSES_SSHKEY_ADD, getOpenSSH(key)))
            } else {
                context.toast("Bluetooth not connected. Could not send key to Pi.")
                return false
            }
        }
        return true
    }

    private fun connect(uriStr: String, isSmartConnect: Boolean) {
        var uriString = uriStr
        if (!uriString.startsWith("ssh://")) uriString = "ssh://$uriString"
        val host = HostBean()
        host.setHostFromUri(Uri.parse(uriString))
        if (isSmartConnect) {
            host.keyName = "SmartConnectKey"
            host.fontSize = 7
        }
        SaveUtils.updateHostList(requireContext(), host)
        logD("HOST URI " + host.uri.toString())
        launchSSH(requireActivity(), host)
    }

    private fun addTextValidation() {
        bind.sshTextInput.addTextChangedListener {
            if (sshPattern.matcher(it.toString()).matches()) {
                bind.sshTextInput.error = null
                setEnabled(true)
            } else {
                bind.sshTextInput.error = "Unknown Format"
                setEnabled(false)
            }
        }
    }

    fun setEnabled(bool: Boolean) {
        bind.connectSsh.isEnabled = bool
        bind.connectSsh.isClickable = bool
        bind.smartConnect.isEnabled = bool
        bind.smartConnect.isClickable = bool
    }

    private fun launchSSH(activity: FragmentActivity, host: HostBean) {
        val contents = Intent(Intent.ACTION_VIEW, host.uri)
        contents.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        contents.setClass(activity, SSHConsole::class.java)
        activity.startActivity(contents)
    }

    private fun getIP(s: String) {
        if (s.contains("eth0")) {
            val ipAddress = s.substringAfterLast("ip: ").trim()
            val hostAddress = "pi@$ipAddress"
            bind.sshTextInput.setText(hostAddress)
            logD("GOT IP $ipAddress")
        } else if (s.contains("ip") || s.startsWith("essid")) {
            val splitResult = s.split(", ")
            if (splitResult.size > 1) {
                val ipString = splitResult[1]
                if (ipString.startsWith("ip:")) {
                    val ipAddress = ipString.substring(4)
                    val hostAddress = "pi@$ipAddress"
                    bind.sshTextInput.setText(hostAddress)
                    logD("GOT IP $ipAddress")
                }
            }
        }
    }
    override fun getMessage(msg: Message) {
        when (msg.what) {
            Constants.MESSAGE_READ -> {
                val output = msg.obj as String
                if (output.isNotEmpty()) getIP(output)
            }
        }
    }
}