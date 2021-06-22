package io.treehouses.remote.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.treehouses.remote.R
import io.treehouses.remote.callback.DeviceDeleteListener
import io.treehouses.remote.pojo.DeviceInfo

class RPIListAdapter(private val mContext: Context, private val data: List<DeviceInfo>) : ArrayAdapter<DeviceInfo?>(mContext, 0, data) {
    public var deviceListener: DeviceDeleteListener? = null
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the data item for this position
        var newView = convertView
        val deviceText = data[position].deviceName

        // Check if an existing view is being reused, otherwise inflate the view
        if (newView == null) {
            newView = LayoutInflater.from(mContext).inflate(R.layout.list_rpi_item, parent, false)
        }
        val text = newView!!.findViewById<TextView>(R.id.device_info)
        val pairedImage = newView.findViewById<ImageView>(R.id.paired_icon)
        val deleteDevice = newView.findViewById<ImageView>(R.id.delete_icon)
        deleteDevice.setOnClickListener { deviceListener?.onDeviceDeleted(position) }
        text.text = deviceText
        pairedImage.visibility = View.INVISIBLE
        deleteDevice.visibility = View.INVISIBLE
        if (data[position].isPaired) {
            pairedImage.visibility = View.VISIBLE
            deleteDevice.visibility = View.VISIBLE
            pairedImage.setColorFilter(getTint(data[position]))
            deleteDevice.setColorFilter(getTint(data[position]))
        }

        // Return the completed view to render on screen
        return newView
    }

    private fun getTint(deviceInfo: DeviceInfo): Int {
        return if (deviceInfo.isInRange) ContextCompat.getColor(mContext, R.color.md_green_500) else ContextCompat.getColor(mContext, R.color.md_grey_400)
    }


}