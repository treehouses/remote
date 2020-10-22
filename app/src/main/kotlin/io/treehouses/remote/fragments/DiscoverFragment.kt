package io.treehouses.remote.fragments

import android.content.Context.WIFI_SERVICE
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Message
import android.text.format.Formatter.formatIpAddress
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.parse.Parse.getApplicationContext
import io.treehouses.remote.Constants
import io.treehouses.remote.fragments.dialogfragments.RPIDialogFragment
import io.treehouses.remote.interfaces.FragmentDialogInterface
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityDiscoverFragmentBinding
import io.treehouses.remote.utils.logD
import io.treehouses.remote.utils.logE
import kotlinx.android.synthetic.main.activity_discover_fragment.view.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


class DiscoverFragment : BaseFragment(), FragmentDialogInterface {
    private lateinit var bind: ActivityDiscoverFragmentBinding
    private var gateway = Gateway()
    private var pi = Device()
    private var piIP = ""
    private var deviceList = ArrayList<Device>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityDiscoverFragmentBinding.inflate(inflater, container, false)
        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)
        load()
        return bind.root
    }

    private fun requestNetworkInfo() {
        logD("$TAG, Requesting Network Information")
        try {
            listener.sendMessage(getString(R.string.TREEHOUSES_DISCOVER_GATEWAY_LIST))
            listener.sendMessage(getString(R.string.TREEHOUSES_DISCOVER_GATEWAY))
            listener.sendMessage(getString(R.string.TREEHOUSES_DISCOVER_SELF))
        } catch (e: Exception) {
            logE("Error Requesting Network Information")
        }
    }

    private fun setupIcons() {
        bind.deviceContainer.removeAllViewsInLayout()

        val midX = (bind.deviceContainer.measuredWidth / 2).toFloat()
        val midY = (bind.deviceContainer.measuredHeight / 2).toFloat()

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
        val wifiMgr = getApplicationContext().getSystemService(WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiMgr.connectionInfo
        val ipAddress = formatIpAddress(wifiInfo.ipAddress)
        val imageView = ImageView(context)

        if (d.ip == ipAddress) imageView.setImageResource(R.drawable.android_icon)
        else if (d.ip == piIP) imageView.setImageResource(R.drawable.treehouses_rounded)
        else if (RPIDialogFragment.checkPiAddress(d.mac)) imageView.setImageResource(R.drawable.raspi_logo)
        else imageView.setImageResource(R.drawable.circle_yellow)
        imageView.layoutParams = LinearLayout.LayoutParams(size, size)
        imageView.x = x
        imageView.y = y

        imageView.setOnClickListener {
            val message = ("IP Address: " + d.ip + "\n") +
                    ("MAC Address: " + d.mac)
            message.lines()
            showDialog(context, "Device Information", message)
        }

        bind.deviceContainer.addView(imageView)
    }

    private fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float) {
        val bitmap = Bitmap.createBitmap(
                bind.deviceContainer.measuredWidth,
                bind.deviceContainer.measuredHeight,
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
        bind.deviceContainer.addView(line)
    }

    private fun updateGatewayIcon() {
        val gatewayIcon = bind.gatewayContainer.gateway_icon
        bind.gatewayContainer.removeView(gatewayIcon)


        if (gateway.isComplete()) {
            gatewayIcon.visibility = View.VISIBLE
            gatewayIcon.setOnClickListener {
                val message = ("SSID: " + gateway.ssid + "\n") +
                        ("IP Address: " + gateway.device.ip + "\n") +
                        ("MAC Address: " + gateway.device.mac + "\n") +
                        ("Connected Devices: " + (deviceList.size - 1))
                message.lines()
                showDialog(context, "Gateway Information", message)
            }
        }
        bind.gatewayContainer.addView(gatewayIcon)
    }

    private fun addDevices(readMessage: String): Boolean {
        var regex = "([0-9]+\\.){3}[0-9]+\\s+([0-9A-Z]+:){5}[0-9A-Z]+".toRegex()
        val devices = regex.findAll(readMessage)

        devices.forEach {
            val device = Device()

            device.ip = it.value.split("\\s+".toRegex())[0]
            device.mac = it.value.split("\\s+".toRegex())[1]

            if (!deviceList.contains(device)) deviceList.add(device)
        }

        return !devices.none()
    }

    private fun updatePiInfo(readMessage: String): Boolean {
        val ip = extractText("([0-9]+\\.){3}[0-9]+", "", readMessage)

        if (ip != null) {
            pi.ip = ip
            piIP = ip
        }

        val mac1 = extractText("eth0:\\s+([0-9a-z]+:){5}[0-9a-z]+", "eth0:\\s+", readMessage)
        val mac2 = extractText("wlan0:\\s+([0-9a-z]+:){5}[0-9a-z]+", "wlan0:\\s+", readMessage)

        if (mac1 != null) pi.mac = "\n$mac1 (ethernet)\n"

        if (mac2 != null) pi.mac += "$mac2 (wlan)\n"

        if (pi.isComplete() && pi.mac.matches("\n(.)+\n(.)+\n".toRegex()))
            if (!deviceList.contains(pi)) deviceList.add(pi)

        return !ip.isNullOrEmpty() || mac1.isNullOrEmpty() || !mac2.isNullOrEmpty()
    }

    private fun updateGatewayInfo(readMessage: String): Boolean {
        val ip = extractText("ip address:\\s+([0-9]+\\.){3}[0-9]", "ip address:\\s+", readMessage)
        if (ip != null) gateway.device.ip = ip

        val ssid = extractText("ESSID:\"(.)+\"", "ESSID:", readMessage)
        if (ssid != null) gateway.ssid = ssid.substring(1, ssid.length - 1)

        val mac = extractText("MAC Address:\\s+([0-9A-Z]+:){5}[0-9A-Z]+", "MAC Address:\\s+", readMessage)
        if (mac != null) gateway.device.mac = mac

        return !ip.isNullOrEmpty() || !ssid.isNullOrEmpty() || !mac.isNullOrEmpty()
    }

    private fun extractText(pattern: String, separator: String, msg: String): String? {
        val regex = pattern.toRegex()
        val res = regex.find(msg)
        var text: String? = null

        if (res != null) {
            if (separator == "") text = res.value
            else text = res.value.split(separator.toRegex())[1]
        }

        return text
    }

    override fun getMessage(msg: Message) {
        when (msg.what) {
            Constants.MESSAGE_WRITE -> {
                val writeMsg = String((msg.obj as ByteArray))
                logD("WRITE $writeMsg")
            }
            Constants.MESSAGE_READ -> {
                val readMessage = msg.obj as String
                logD("$TAG, READ = $readMessage")

                if(!addDevices(readMessage))
                    if(!updateGatewayInfo(readMessage))
                        updatePiInfo(readMessage)

                if (readMessage.startsWith("Ports:")) transition()
            }
        }
    }

    private fun load() {
        bind.loading1.visibility = View.VISIBLE
        bind.loading2.visibility = View.VISIBLE
        bind.deviceContainer.visibility = View.INVISIBLE
        bind.gatewayIcon.visibility = View.INVISIBLE

        deviceList.clear()

        bind.swiperefresh.isRefreshing = false
        bind.swiperefresh.isEnabled = false
        bind.swiperefresh.setOnRefreshListener(null)

        requestNetworkInfo()
    }

    private fun transition() {
        bind.swiperefresh.isEnabled = true
        bind.swiperefresh.setOnRefreshListener {
            load()
        }

        setupIcons()
        if(gateway.isComplete()) updateGatewayIcon()
        else createAlertDialog(requireContext(), 1, "Error", "Unable to fetch gateway info.").setPositiveButton("Dismiss", null).show()
        bind.loading1.visibility = View.GONE
        bind.loading2.visibility = View.GONE
        bind.deviceContainer.visibility = View.VISIBLE
    }

    private fun getSize(): Int {
        return when {
            deviceList.size <= 12 -> ICON_MEDIUM_SIZE
            deviceList.size <= 20 -> ICON_SMALL_SIZE
            else -> ICON_XSMALL_SIZE
        }
    }

    inner class Device {
        lateinit var ip: String
        lateinit var mac: String

        override fun equals(other: Any?): Boolean {
            return this.ip == (other as Device).ip
        }

        fun isComplete(): Boolean {
            return this::ip.isInitialized && this::mac.isInitialized
        }
    }

    inner class Gateway {
        var device = Device()
        lateinit var ssid: String

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
