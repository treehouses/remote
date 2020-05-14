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
import io.treehouses.remote.pojo.DeviceInfo

class RPIListAdapter(context: Context, data: List<DeviceInfo>) : ArrayAdapter<DeviceInfo?>(context, 0, data) {
    private val data: List<DeviceInfo>
    private val context: Context
    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        // Get the data item for this position
        var convertView = convertView
        val deviceText: String = data[position].getDeviceName()
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_rpi_item, parent, false)
        }
        val text = convertView.findViewById<TextView>(R.id.device_info)
        val pairedImage = convertView.findViewById<ImageView>(R.id.paired_icon)
        text.text = deviceText
        pairedImage.visibility = View.INVISIBLE
        if (data[position].isPaired()) {
            pairedImage.visibility = View.VISIBLE
            if (data[position].isInRange()) {
                pairedImage.setColorFilter(ContextCompat.getColor(context, R.color.md_green_500))
            } else {
                pairedImage.setColorFilter(ContextCompat.getColor(context, R.color.md_grey_400))
            }
        }

        // Return the completed view to render on screen
        return convertView
    }

    init {
        this.data = data
        this.context = context
    }
}