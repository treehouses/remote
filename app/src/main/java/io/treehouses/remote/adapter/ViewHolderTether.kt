package io.treehouses.remote.adapter

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


class ViewHolderTether internal constructor(v: View, listener: HomeInteractListener, context: Context) {
    private fun openHotspotSettings(context: Context) {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val cn = ComponentName("com.android.settings", "com.android.settings.TetherSettings")
        intent.component = cn
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun showAlertDialog(context: Context): AlertDialog {
        return AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
                .setTitle("OUTPUT:")
                .setMessage("Hotspot is disabled, open hotspot settings?")
                .setIcon(R.drawable.wificon)
                .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int -> openHotspotSettings(context) }
                .setNegativeButton("NO") { dialog: DialogInterface, _: Int -> dialog.cancel() }.show()
    }

    companion object {
        lateinit var editTextSSID: TextInputEditText
        lateinit var editTextPassword: TextInputEditText

        private fun isApOn(context: Context): Boolean {
            val manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            var actualState = 0
            var method: Method? = null
            try {
                method = manager.javaClass.getDeclaredMethod("getWifiApState")
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }
            method!!.isAccessible = true
            try {
                actualState = method.invoke(manager) as Int
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
            return actualState == 13
        }
    }

    init {
        val imageViewSettings = v.findViewById<ImageView>(R.id.imageViewSettings)
        val btnStartConfig = v.findViewById<Button>(R.id.btn_start_config)
        editTextSSID = v.findViewById(R.id.editTextSSID)
        editTextPassword = v.findViewById(R.id.editTextPassword)
        editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        imageViewSettings.setOnClickListener { openHotspotSettings(context) }
        if (!isApOn(context)) {
            showAlertDialog(context)
        }
        btnStartConfig.setOnClickListener {
            val ssid = editTextSSID.text.toString()
            val password = editTextPassword.text.toString()
            if (ssid.isNotEmpty()) {
                listener.sendMessage("treehouses wifi " + ssid + " " + if (password.isEmpty()) "" else password)
                Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Error: Invalid SSID", Toast.LENGTH_LONG).show()
            }
        }
    }
}