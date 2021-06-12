package io.treehouses.remote.adapter

import android.bluetooth.BluetoothDevice
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
        var device = data[position]
        // Check if an existing view is being reused, otherwise inflate the view
        if (newView == null) {
            newView = LayoutInflater.from(getContext()).inflate(R.layout.list_rpi_item, parent, false)
        }
        val text = newView!!.findViewById<TextView>(R.id.device_info)
        val pairedImage = newView.findViewById<ImageView>(R.id.paired_icon)
        val deleteDevice = newView.findViewById<ImageView>(R.id.delete_icon)
        deleteDevice.setOnClickListener{
            deviceListener?.onDeviceDeleted(position)
        }
        text.text = deviceText
        pairedImage.visibility = View.INVISIBLE
        deleteDevice.visibility = View.INVISIBLE
        if (data[position].isPaired) {
            pairedImage.visibility = View.VISIBLE
            deleteDevice.visibility = View.VISIBLE
            if (data[position].isInRange) {
                pairedImage.setColorFilter(ContextCompat.getColor(mContext, R.color.md_green_500))
                deleteDevice.setColorFilter(ContextCompat.getColor(mContext, R.color.md_green_500))
            } else {
                pairedImage.setColorFilter(ContextCompat.getColor(mContext, R.color.md_grey_400))
                deleteDevice.setColorFilter(ContextCompat.getColor(mContext, R.color.md_grey_400))
            }
        }

        // Return the completed view to render on screen
        return newView
    }


}