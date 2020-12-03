package io.treehouses.remote.ui.discover

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Message
import android.text.AutoText.getSize
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.parse.Parse
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityDiscoverFragmentBinding
import io.treehouses.remote.fragments.DiscoverFragment
import io.treehouses.remote.fragments.dialogfragments.RPIDialogFragment
import io.treehouses.remote.interfaces.FragmentDialogInterface
import io.treehouses.remote.utils.logD
import kotlinx.android.synthetic.main.activity_discover_fragment.view.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class DiscoverFragment : BaseFragment(), FragmentDialogInterface {

    protected val viewModel: DiscoverViewModel by viewModels(ownerProducer = {this})
    private lateinit var bind: ActivityDiscoverFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityDiscoverFragmentBinding.inflate(inflater, container, false)
        viewModel.onLoad()
        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)

        return bind.root
    }

//    private fun transition()
//    {
//        setupIcons()
//        if(viewModel.gateway.isComplete()) updateGatewayIcon()
//        else createAlertDialog(requireContext(), 1, "Error", "Unable to fetch gateway info.").setPositiveButton("Dismiss", null).show()
//        bind.swiperefresh.setOnRefreshListener {
//            viewModel.onLoad()
//        }
//        viewModel.onTransition()
//
//    }

    fun gatewayIconObservers()
    {

    }


    private fun updateGatewayIcon() {

        viewModel.gatewayInformation.observe(viewLifecycleOwner, Observer
        {
            var gateway = it
            val gatewayIcon = bind.gatewayContainer.gateway_icon
            bind.gatewayContainer.removeView(gatewayIcon)
            if (it.isComplete()) {
                gatewayIcon.visibility = View.VISIBLE
                gatewayIcon.setOnClickListener {

                    message.lines()
                    showDialog(context, "Gateway Information", message)
                }
            }

            bind.gatewayContainer.addView(gatewayIcon)

        })





    }


//Move a lot

    private fun setupIcons() {
        bind.deviceContainer.removeAllViewsInLayout()

        val midX = (bind.deviceContainer.measuredWidth / 2).toFloat()
        val midY = (bind.deviceContainer.measuredHeight / 2).toFloat()

        val r = this.resources.displayMetrics.widthPixels / 2 * 0.72
        val interval = 2 * PI / (viewModel.deviceList.size - 1)
        var radians = Random.nextFloat() * 2 * PI

        val size: Int = getSize()

        for (idx in 1 until viewModel.deviceList.size) {
            val d = viewModel.deviceList[idx]

            val x = midX + (r * sin(radians)).toFloat() - size / 2
            val y = midY + (r * -cos(radians)).toFloat() - size / 2

            drawLine(midX, midY, x + size / 2, y + size / 2)
            addIcon(x, y, size, d)

            radians = (radians + interval) % (2 * PI)
        }
    }




    private fun addIcon(x: Float, y: Float, size: Int, d: Device) {
        val wifiMgr = Parse.getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiMgr.connectionInfo
        val ipAddress = Formatter.formatIpAddress(wifiInfo.ipAddress)
        val imageView = ImageView(context)

        if (d.ip == ipAddress) imageView.setImageResource(R.drawable.android_icon)
        else if (d.ip == viewModel.piIP) imageView.setImageResource(R.drawable.treehouses_rounded)
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
    }



    private fun getSize(): Int {
        return when {
            viewModel.deviceList.size <= 12 -> Constants.ICON_MEDIUM_SIZE
            viewModel.deviceList.size <= 20 -> Constants.ICON_SMALL_SIZE
            else -> Constants.ICON_XSMALL_SIZE
        }
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



}