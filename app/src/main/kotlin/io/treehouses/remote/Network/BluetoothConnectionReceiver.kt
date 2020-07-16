package io.treehouses.remote.Network

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BluetoothConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.i("Broadcast Listened", "Bluetooth Service tried to stop")
        when (intent?.action) {
            "restartservice" -> {
                val newIntent = Intent(context, BluetoothChatService::class.java)
                if (intent.hasExtra("DEVICE")) newIntent.putExtra("DEVICE", intent.getParcelableExtra<BluetoothDevice>("DEVICE"))
                Log.e("STARTED", "AGAIN")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(newIntent)
                } else {
                    context.startService(newIntent)
                }
            }
        }
    }
}