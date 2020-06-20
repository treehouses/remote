package io.treehouses.remote.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import io.treehouses.remote.R
import io.treehouses.remote.pojo.ServiceInfo
import java.util.*

class ServicesListAdapter //private Button start, install, restart, link, info;
(private val mContext: Context, private val data: ArrayList<ServiceInfo>, private val headerColour: Int) : ArrayAdapter<ServiceInfo>(mContext, 0, data) {
    private var name: TextView? = null
    private var status: ImageView? = null
    override fun getItem(position: Int): ServiceInfo {
        return data[position]
    }

    override fun getPosition(item: ServiceInfo?): Int {
        for (i in data.indices) {
            if (data[i].name == item!!.name && data[i].serviceStatus == item!!.serviceStatus) {
                return i
            }
        }
        return -1
    }

    override fun notifyDataSetChanged() {
        Collections.sort(data)
        super.notifyDataSetChanged()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun isEnabled(position: Int): Boolean {
        return data[position].serviceStatus != ServiceInfo.SERVICE_HEADER_INSTALLED && data[position].serviceStatus != ServiceInfo.SERVICE_HEADER_AVAILABLE
    }

    private fun findViews(view: View) {
        name = view.findViewById(R.id.service_name)
        status = view.findViewById(R.id.service_status)
        name!!.setTextColor(headerColour)
    }

    private fun setStatus(statusCode: Int) {
        if (statusCode == ServiceInfo.SERVICE_AVAILABLE) {

            //setButtons(false, false, false);
            name!!.setTextColor(context.resources.getColor(R.color.md_grey_600))
            status!!.setImageDrawable(context.resources.getDrawable(R.drawable.circle_red))
        } else if (statusCode == ServiceInfo.SERVICE_INSTALLED) {

            //setButtons(false, true, false);
            name!!.setTextColor(context.resources.getColor(R.color.md_grey_600))
            status!!.setImageDrawable(context.resources.getDrawable(R.drawable.circle_yellow))
        } else if (statusCode == ServiceInfo.SERVICE_RUNNING) {
            //setButtons(true, true, true);
            name!!.setTextColor(context.resources.getColor(R.color.md_green_500))
            status!!.setImageDrawable(context.resources.getDrawable(R.drawable.circle_green))
        }
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        convertView = if (data[position].serviceStatus != ServiceInfo.SERVICE_HEADER_AVAILABLE && data[position].serviceStatus != ServiceInfo.SERVICE_HEADER_INSTALLED) {
            LayoutInflater.from(context).inflate(R.layout.services_row_layout, parent, false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.services_section_header, parent, false)
        }
        findViews(convertView)
        name!!.text = data[position].name
        setStatus(data[position].serviceStatus)
        return convertView
    }

}