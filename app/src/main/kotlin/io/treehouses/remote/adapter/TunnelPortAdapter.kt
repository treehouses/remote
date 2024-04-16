package io.treehouses.remote.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import io.treehouses.remote.R


class TunnelPortAdapter(private val mContext: Context, private val data: List<String?>) : ArrayAdapter<String?>(mContext, 0, data) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val convertedView: View
        val deleteAllPortsButtonSelected = data.size > 1 && position == data.size-1
        convertedView = if (deleteAllPortsButtonSelected) LayoutInflater.from(mContext).inflate(R.layout.select_dialog_item_delete_all, parent, false)
        else LayoutInflater.from(mContext).inflate(R.layout.select_dialog_item, parent, false)
        val text = convertedView as TextView
        text.text = getItem(position)
        // Return the completed view to render on screen
        return convertedView
    }
}