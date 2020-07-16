package io.treehouses.remote.Fragments.DialogFragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import io.treehouses.remote.Fragments.SSHConsole
import io.treehouses.remote.SSH.beans.HostBean
import io.treehouses.remote.bases.FullScreenDialogFragment
import io.treehouses.remote.databinding.DialogSshBinding
import java.util.regex.Pattern


class SSHDialog : FullScreenDialogFragment() {
    private val sshPattern = Pattern.compile("^(.+)@(([0-9a-z.-]+)|(\\[[a-f:0-9]+\\]))(:(\\d+))?$", Pattern.CASE_INSENSITIVE)
    private lateinit var bind: DialogSshBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogSshBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEnabled(false)
        addTextValidation()
        bind.connectSsh.setOnClickListener {
            launchSSH(requireActivity(), bind.sshTextInput.text.toString().split("@")[0], bind.sshTextInput.text.toString().split("@")[1])
        }
    }

    private fun addTextValidation() {
        bind.sshTextInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (sshPattern.matcher(s).matches()) {
                    bind.sshTextInput.error = null
                    setEnabled(true)
                }
                else {
                    bind.sshTextInput.error = "Unknown Format"
                    setEnabled(false)
                }
            }

        })
    }

    fun setEnabled(bool: Boolean) {
        bind.connectSsh.isEnabled = bool
        bind.connectSsh.isClickable = bool
    }

    private fun launchSSH(activity: FragmentActivity, username: String, hostname: String) {
        val host = HostBean()
        host.username = username
        host.hostname = hostname
        val contents = Intent(Intent.ACTION_VIEW, host.uri)
        contents.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        contents.setClass(activity, SSHConsole::class.java)
        activity.startActivity(contents)
        dialog?.dismiss()
    }
}