package io.treehouses.remote.Fragments

import android.graphics.*
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.getColor
import io.treehouses.remote.Constants
import io.treehouses.remote.Interfaces.FragmentDialogInterface
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.BackPressReceiver
import io.treehouses.remote.databinding.ActivityDiscoverFragmentBinding
import kotlinx.android.synthetic.main.activity_discover_fragment.view.*
import java.lang.Exception
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class DiscoverFragment : BaseFragment(), FragmentDialogInterface {
    private lateinit var bind : ActivityDiscoverFragmentBinding
    private var gateway = Gateway()
    private var deviceList = ArrayList<Device>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityDiscoverFragmentBinding.inflate(inflater, container, false)
        bind.progressBar.visibility = View.VISIBLE
        bind.container.visibility = View.INVISIBLE
        deviceList.clear()
        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)
        requestNetworkInfo()
        return bind.root
    }

    private fun requestNetworkInfo() {
        Log.d(TAG, "Requesting Network Information")
        try {
            listener.sendMessage(getString(R.string.TREEHOUSES_DISCOVER_GATEWAY_LIST))
            listener.sendMessage(getString(R.string.TREEHOUSES_DISCOVER_GATEWAY))
        }
        catch (e : Exception) {
            Log.e(TAG, "Error Requesting Network Information")
        }
    }

    private fun setupIcons() {
        bind.container.removeAllViewsInLayout()

        val midX = (bind.container.measuredWidth / 2).toFloat()
        val midY = (bind.container.measuredHeight / 2).toFloat()

        val r = this.resources.displayMetrics.widthPixels / 2 * 0.72
        val interval = 2 * PI / (deviceList.size - 1)
        var radians = Random.nextFloat() * 2 * PI

        val size: Int = getSize()

        for (idx in 1 until deviceList.size) {
            val d = deviceList[idx]

            val x = midX + (r * sin(radians)).toFloat() - size / 2
            val y = midY + (r * -cos(radians)).toFloat() - size / 2

            drawLine(midX, midY, x + size / 2, y + size / 2)
            addIcon(x, y, size, d)

            radians = (radians + interval) % (2 * PI)
        }
    }

    private fun addIcon(x: Float, y: Float, size: Int, d: Device) {
        val imageView = ImageView(context)
        imageView.setImageResource(R.drawable.circle_yellow)
        imageView.layoutParams = LinearLayout.LayoutParams(size, size)
        imageView.x = x
        imageView.y = y

        imageView.setOnClickListener {
            val message = ("IP Address: " + d.ip + "\n") +
                    ("MAC Address: " + d.mac)
            message.lines()
            showDialog(context, "Device Information", message)
        }

        bind.container.addView(imageView)
    }

    private fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float) {
        val bitmap = Bitmap.createBitmap(
                bind.container.measuredWidth,
                bind.container.measuredHeight,
                Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        val p4 = Paint()
        p4.isAntiAlias = true
        p4.color = resources.getColor(R.color.daynight_textColor)
        p4.strokeWidth = 10f
        canvas.drawLine(startX, startY, endX, endY, p4)

        val line = ImageView(context)
        line.setImageBitmap(bitmap)
        bind.container.addView(line)
    }

    private fun updateGatewayIcon() {
        val gatewayIcon = bind.iconContainer.icon
        bind.iconContainer.removeView(gatewayIcon)
        gatewayIcon.setOnClickListener {
            val message = ("SSID: " + gateway.ssid + "\n") +
                    ("IP Address: " + gateway.device.ip + "\n") +
                    ("MAC Address: " + gateway.device.mac + "\n") +
                    ("Connected Devices: " + (deviceList.size - 1))

            message.lines()
            showDialog(context,"Gateway Information", message)
        }
        if(gateway.isComplete()) {
            gatewayIcon.visibility = View.VISIBLE
            bind.container.visibility = View.VISIBLE
            bind.progressBar.visibility = View.GONE
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

    private fun updateGatewayInfo(readMessage: String) : Boolean {
        val ip = extractText("ip address:\\s+([0-9]+.){3}[0-9]", "ip address:\\s+", readMessage)
        if(ip != null) {
            gateway.device.ip = ip
        }

        val ssid = extractText("ESSID:\"(.)+\"", "ESSID:", readMessage)
        if (ssid != null) {
            gateway.ssid = ssid.substring(1, ssid.length - 1)
        }

        val mac = extractText("MAC Address:\\s+([0-9A-Z]+:){5}[0-9A-Z]+", "MAC Address:\\s+", readMessage)
        if (mac != null) {
            gateway.device.mac = mac
        }

        return ip.isNullOrEmpty() || ssid.isNullOrEmpty() || mac.isNullOrEmpty()
    }

    private fun extractText(pattern: String, separator: String, msg: String): String? {
        val regex = pattern.toRegex()
        val res = regex.find(msg)
        var text: String? = null

        if(res != null)
            text = res.value.split(separator.toRegex())[1]

        return text
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
                else if (updateGatewayInfo(readMessage))
                    updateGatewayIcon()
            }
        }
    }

    private fun getSize(): Int {
        return when {
            deviceList.size <= 12 -> ICON_MEDIUM_SIZE
            deviceList.size <= 20 -> ICON_SMALL_SIZE
            else -> ICON_XSMALL_SIZE
        }
    }

    inner class Device {
        lateinit var ip : String
        lateinit var mac : String

        override fun equals(other : Any?) : Boolean {
            return this.ip == (other as Device).ip
        }

        fun isComplete(): Boolean {
            return this::ip.isInitialized && this::mac.isInitialized
        }
    }

    inner class Gateway {
        var device = Device()
        lateinit var ssid : String

        fun isComplete(): Boolean {
            return device.isComplete() && this::ssid.isInitialized
        }
    }

    companion object {
        private const val TAG = "Discover Fragment"
        private const val ICON_MEDIUM_SIZE = 200
        private const val ICON_SMALL_SIZE = 120
        private const val ICON_XSMALL_SIZE = 80
    }
}
