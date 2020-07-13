package io.treehouses.remote.Fragments.DialogFragments

import android.app.AlertDialog
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import io.treehouses.remote.Fragments.SSHConsole
import io.treehouses.remote.R
import io.treehouses.remote.SSH.beans.HostBean
import io.treehouses.remote.databinding.DialogSshBinding
import java.util.regex.Pattern

class SSHDialog(activity: AppCompatActivity) {
    private val sshPattern = Pattern.compile("^(.+)@(([0-9a-z.-]+)|(\\[[a-f:0-9]+\\]))(:(\\d+))?$", Pattern.CASE_INSENSITIVE)
    private var bind: DialogSshBinding = DialogSshBinding.inflate(activity.layoutInflater)
    private var dialog: AlertDialog? = null
    init {
        setEnabled(false)
        addTextValidation()
        bind.connectSsh.setOnClickListener {
            launchSSH(activity, bind.sshTextInput.text.toString().split("@")[0], bind.sshTextInput.text.toString().split("@")[1])
            dialog?.dismiss()
        }
        dialog = AlertDialog.Builder(activity).setTitle("Start SSH Connection").setView(bind.root).setIcon(R.drawable.dialog_icon).create()
        dialog?.show()
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

    private fun launchSSH(activity: AppCompatActivity, username: String, hostname: String) {
        val host = HostBean()
        host.username = username
        host.hostname = hostname
        val contents = Intent(Intent.ACTION_VIEW, host.uri)
        contents.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        contents.setClass(activity, SSHConsole::class.java)
        activity.startActivity(contents)
    }
}