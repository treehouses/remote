package io.treehouses.remote.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.os.Bundle
import android.os.Handler
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
import io.treehouses.remote.Views.DiscoverViewPager
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityDiscoverNetworkFragmentBinding
import java.lang.Exception
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class DiscoverNetworkFragment : BaseFragment() {
    private lateinit var bind : ActivityDiscoverNetworkFragmentBinding
    private lateinit var viewPager : DiscoverViewPager
    private var deviceList = ArrayList<Device>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityDiscoverNetworkFragmentBinding.inflate(inflater, container, false)
        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)
        requestGatewayList()
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupIcons() {
        bind.container.removeAllViews()

        val icon = ImageView(context)
        icon.setImageResource(R.drawable.ic_download_icon)
        val param = LinearLayout.LayoutParams(200, 200)

        icon.layoutParams = param

        val midX = (this.resources.displayMetrics.widthPixels / 2).toFloat()
        val midY = (this.resources.displayMetrics.heightPixels / 2 - 100).toFloat()

        icon.x = midX - 100
        icon.y = midY - 100

        val r = this.resources.displayMetrics.widthPixels / 2 * 0.7
        val interval = 2 * PI / deviceList.size
        var radians = 0.0

        for(i in deviceList) {
            val imageView = ImageView(context)
            imageView.setImageResource(R.drawable.circle_yellow)
            imageView.layoutParams = LinearLayout.LayoutParams(200, 200)
            imageView.x = midX + (r * sin(radians)).toFloat() - 100
            imageView.y = midY + (r * -cos(radians)).toFloat() - 100

            imageView.setOnClickListener {
                val message = ("IP Address: " + i.ip + "\n") +
                        ("MAC Address: " + i.mac)
                message.lines()
                showDialog("Device Information", message)
            }

            val bitmap = Bitmap.createBitmap(
                    this.resources.displayMetrics.widthPixels,
                    this.resources.displayMetrics.heightPixels,
                    Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            val p4 = Paint()
            p4.isAntiAlias = true
            p4.color = Color.BLACK
            p4.strokeWidth = 10f
            canvas.drawLine(midX, (midY + 100), (imageView.x + 100),(imageView.y + 200), p4)

            radians += interval

            val line = ImageView(context)
            line.setImageBitmap(bitmap)
            bind.container.addView(line)
            bind.container.addView(imageView)
        }
        bind.container.addView(icon)
    }

    private fun showDialog(title: String, message: String) {
        val alertDialog = CreateAlertDialog(context, R.style.CustomAlertDialogStyle,title,message)
                .setPositiveButton("OK") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
        alertDialog.show()
    }

    private fun CreateAlertDialog(context: Context?, id:Int, title:String, message:String): AlertDialog.Builder {
        return AlertDialog.Builder(ContextThemeWrapper(context, id))
                .setTitle(title)
                .setMessage(message)
    }

    private fun requestGatewayList() {
        Log.e(TAG, "Requesting Gateway List")
        try {
            listener.sendMessage(getString(R.string.TREEHOUSES_DISCOVER_GATEWAY_LIST))
        }
        catch (e : Exception) {
            Log.e(TAG, "Failed bro")
        }
        setupIcons()
    }

    private fun getGatewayList(message: String) {
        Log.e(TAG, "MBA")
//        deviceList.clear()
        val splitMessage = message.lines()

        for(m in splitMessage) {
            val device = Device()

            device.ip = m.split("\\s+".toRegex())[0]
            device.mac = m.split("\\s+".toRegex())[1]

            deviceList.add(device)
        }

        setupIcons()
    }

    val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_WRITE -> {
                    val writeMsg = String((msg.obj as ByteArray))
                    Log.e("WRITE", writeMsg)
                }
                Constants.MESSAGE_READ -> {
                    val readMessage = msg.obj as String
                    Log.e(TAG, "readMessage = $readMessage")
                    val regex = "([0-9]+.){3}[0-9]+\\s+([0-9a-z]+:){5}[0-9a-z]+".toRegex()
                    val collection = regex.findAll(readMessage)

                    collection.forEach {
                        Log.e(TAG, "Regex match: " + it.value)
                        val device = Device()

                        device.ip = it.value.split("\\s+".toRegex())[0]
                        device.mac = it.value.split("\\s+".toRegex())[1]

                        deviceList.add(device)
                    }

                    setupIcons()
                }
            }
        }
    }

    inner class Device {
        lateinit var ip : String
        lateinit var mac : String
    }

    companion object {
        private const val TAG = "Discover Fragment"
    }
}