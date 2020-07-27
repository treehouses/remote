package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.media.Image
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityDiscoverFragmentBinding
import kotlinx.android.synthetic.main.activity_discover_fragment.view.*
import java.lang.Exception
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class DiscoverFragment : BaseFragment() {
    private lateinit var bind : ActivityDiscoverFragmentBinding
    private var gateway = Gateway()
    private var deviceList = ArrayList<Device>()
    private lateinit var gatewayIcon : ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityDiscoverFragmentBinding.inflate(inflater, container, false)
        deviceList.clear()
        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)
        requestNetworkList()
        requestGatewayInfo()
        return bind.root
    }

    private fun setupIcons() {
        bind.container.removeAllViewsInLayout()

        val midX = (bind.container.measuredWidth / 2).toFloat()
        val midY = (bind.container.measuredHeight / 2).toFloat()

        val r = this.resources.displayMetrics.widthPixels / 2 * 0.72
        val interval = 2 * PI / (deviceList.size - 1)
        var radians = Random.nextFloat() * 2 * PI

        var size: Int = when {
            deviceList.size <= 12 -> ICON_MEDIUM_SIZE
            deviceList.size <= 20 -> ICON_SMALL_SIZE
            else -> ICON_XSMALL_SIZE
        }

        for(idx in 1 until deviceList.size) {
            val i = deviceList[idx]
            val imageView = ImageView(context)
            imageView.setImageResource(R.drawable.circle_yellow)
            imageView.layoutParams = LinearLayout.LayoutParams(size, size)
            imageView.x = midX + (r * sin(radians)).toFloat() - size / 2
            imageView.y = midY + (r * -cos(radians)).toFloat() - size / 2

            imageView.setOnClickListener {
                val message = ("IP Address: " + i.ip + "\n") +
                        ("MAC Address: " + i.mac)
                message.lines()
                showDialog("Device Information", message)
            }

            val bitmap = Bitmap.createBitmap(
                bind.container.measuredWidth,
                bind.container.measuredHeight,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            val p4 = Paint()
            p4.isAntiAlias = true
            p4.color = Color.BLACK
            p4.strokeWidth = 10f
            canvas.drawLine(midX, midY, imageView.x + size / 2, imageView.y + size / 2, p4)

            radians = (radians + interval) % (2 * PI)

            val line = ImageView(context)
            line.setImageBitmap(bitmap)
            bind.container.addView(line)
            bind.container.addView(imageView)
        }

        size = ICON_MEDIUM_SIZE

        val icon = ImageView(context)
        icon.setImageResource(R.drawable.ic_bluetooth)
        val param = LinearLayout.LayoutParams(size, size)

        icon.x = midX - size / 2
        icon.y = midY - size / 2

        icon.layoutParams = param
        gatewayIcon = icon

//        bind.container.addView(icon)
    }

    private fun showDialog(title: String, message: String) {
        val alertDialog = CreateAlertDialog(context, R.style.CustomAlertDialogStyle,title,message)
                .setPositiveButton("OK") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    private fun CreateAlertDialog(context: Context?, id:Int, title:String, message:String): AlertDialog.Builder {
        return AlertDialog.Builder(ContextThemeWrapper(context, id))
                .setTitle(title)
                .setMessage(message)
    }

    private fun requestNetworkList() {
        Log.d(TAG, "Requesting Network List")
        try {
            listener.sendMessage(getString(R.string.TREEHOUSES_DISCOVER_GATEWAY_LIST))
        }
        catch (e : Exception) {
            Log.e(TAG, "Error Requesting Network List")
        }
    }

    private fun requestGatewayInfo() {
        try {
            listener.sendMessage(getString(R.string.TREEHOUSES_DISCOVER_GATEWAY))
        }
        catch (e : Exception) {
            Log.e(TAG, "Error Requesting Gateway Information")
        }
    }

    private fun updateGatewayIcon() {
        gatewayIcon = bind.iconContainer.icon
        bind.iconContainer.removeView(gatewayIcon)
        gatewayIcon.setOnClickListener {
            val message = ("SSID: " + gateway.ssid + "\n") +
                    ("IP Address: " + gateway.device.ip + "\n") +
                    ("MAC Address: " + gateway.device.mac + "\n") +
                    ("Connected Devices: " + (deviceList.size - 1))

            message.lines()
            showDialog("Gateway Information", message)
        }
        bind.iconContainer.addView(gatewayIcon)
    }

    private fun addDevices(readMessage : String) : Boolean {
        val regex = "([0-9]+.){3}[0-9]+\\s+([0-9A-Z]+:){5}[0-9A-Z]+".toRegex()
        val devices = regex.findAll(readMessage)

        devices.forEach {
            val device = Device()

            device.ip = it.value.split("\\s+".toRegex())[0]
            device.mac = it.value.split("\\s+".toRegex())[1]

            if(!deviceList.contains(device))
                deviceList.add(device)
        }

        return !devices.none()
    }

    private fun addGatewayInformation(readMessage: String) : Boolean {
        var regex : Regex = "ip address:\\s+([0-9]+.){3}[0-9]".toRegex()
        val ip = regex.find(readMessage)
        if (ip != null) {
            gateway.device.ip = ip.value.split("ip address:\\s+".toRegex())[1]
        }

        regex = "ESSID:\"(.)+\"".toRegex()
        val ssid = regex.find(readMessage)
        if (ssid != null) {
            var trimmedSsid = ssid.value.split("ESSID:".toRegex())[1]
            trimmedSsid = trimmedSsid.substring(1, trimmedSsid.length - 1)
            gateway.ssid = trimmedSsid
        }

        regex = "MAC Address:\\s+([0-9A-Z]+:){5}[0-9A-Z]+".toRegex()
        val mac = regex.find(readMessage)
        if (mac != null) {
            gateway.device.mac = mac.value.split("MAC Address:\\s+".toRegex())[1]
        }

        return (ip != null) || (ssid != null) || (mac != null)
    }

    override fun getMessage(msg: Message) {
        when (msg.what) {
            Constants.MESSAGE_WRITE -> {
                val writeMsg = String((msg.obj as ByteArray))
                Log.d("WRITE", writeMsg)
            }
            Constants.MESSAGE_READ -> {
                val readMessage = msg.obj as String
                Log.d(TAG, "READ = $readMessage")

                if(addDevices(readMessage))
                    setupIcons()
                else if (addGatewayInformation(readMessage))
                    updateGatewayIcon()
            }
        }
    }

    inner class Device {
        lateinit var ip : String
        lateinit var mac : String

        override fun equals(other : Any?) : Boolean {
            return this.ip == (other as Device).ip
        }
    }

    inner class Gateway {
        var device = Device()
        lateinit var ssid : String
    }

    companion object {
        private const val TAG = "Discover Fragment"
        private const val ICON_MEDIUM_SIZE = 200
        private const val ICON_SMALL_SIZE = 120
        private const val ICON_XSMALL_SIZE = 80
    }
}
