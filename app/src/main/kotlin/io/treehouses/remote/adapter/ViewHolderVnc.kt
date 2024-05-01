package io.treehouses.remote.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.utils.Utils.checkAppIsInstalled

class ViewHolderVnc internal constructor(v: View, context: Context, listener: HomeInteractListener) {
    private fun openVnc(context: Context, v: View, `in`: TextInputEditText) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(String.format("vnc://%s:5900", "192.168.1.1")))

        if (checkAppIsInstalled(context, v, intent, arrayOf("No VNC Client installed on you device", "Install", "https://play.google.com/store/apps/details?id=com.realvnc.viewer.android"))) return

        val ip = `in`.text.toString()
        if (TextUtils.isEmpty(ip)) {
            Toast.makeText(context, "Invalid ip address", Toast.LENGTH_LONG).show()
            return
        }
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(String.format("vnc://%s:5900", ip))))
        } catch (e: Exception) {
        }
    }

    companion object {
        lateinit var editTextIp: TextInputEditText
        lateinit var vnc:Switch
    }

    init {
        listener.sendMessage(context.getString(R.string.TREEHOUSES_VNC))
        val btnStartConfig = v.findViewById<Button>(R.id.btn_start_config)
        vnc = v.findViewById(R.id.switchVnc)
        editTextIp = v.findViewById(R.id.editTextIp)
        btnStartConfig.setOnClickListener { openVnc(context, v, editTextIp) }
        vnc.setOnClickListener {
            if (vnc.isChecked) {
                listener.sendMessage(context.resources.getString(R.string.TREEHOUSES_VNC_ON))
                Toast.makeText(context, "Connecting...", Toast.LENGTH_SHORT).show()
            } else {
                listener.sendMessage(context.resources.getString(R.string.TREEHOUSES_VNC_OFF))
            }
        }
    }
}