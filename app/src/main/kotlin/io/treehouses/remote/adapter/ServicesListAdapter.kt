package io.treehouses.remote.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import io.treehouses.remote.R
import io.treehouses.remote.pojo.ServiceInfo
import java.util.*

class ServicesListAdapter //private Button start, install, restart, link, info;
(private val mContext: Context, private val dataIn: ArrayList<ServiceInfo>, private val headerColour: Int) : ArrayAdapter<ServiceInfo>(mContext, 0, dataIn) {
    var data:ArrayList<ServiceInfo> =  dataIn
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
        data.sort()
        super.notifyDataSetChanged()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }


    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun isEnabled(position: Int): Boolean {
        return flag(position)
    }

    private fun findViews(view: View) {
        name = view.findViewById(R.id.service_name)
        status = view.findViewById(R.id.service_status)
        name!!.setTextColor(headerColour)
    }

    private fun setStatus(statusCode: Int) {
        var color = 0
        var drawable = 0
        if(statusCode == ServiceInfo.SERVICE_AVAILABLE || statusCode == ServiceInfo.SERVICE_INSTALLED || statusCode == ServiceInfo.SERVICE_RUNNING){
            when (statusCode) {
                ServiceInfo.SERVICE_AVAILABLE -> {
                    color = R.color.md_grey_600
                    drawable = R.drawable.circle_red
                }
                ServiceInfo.SERVICE_INSTALLED -> {
                    color = R.color.md_grey_600
                    drawable = R.drawable.circle_yellow
                }
                ServiceInfo.SERVICE_RUNNING -> {
                    color = R.color.md_green_500
                    drawable = R.drawable.circle_green
                }
            }
            name!!.setTextColor(context.resources.getColor(color))
            status!!.setImageDrawable(context.resources.getDrawable(drawable))
        }

    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {
        val convertView: View?
        convertView = if (flag(position)) {
            LayoutInflater.from(context).inflate(R.layout.services_row_layout, parent, false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.services_section_header, parent, false)
        }
        findViews(convertView)
        try {
            name!!.text = data[position].name
            setStatus(data[position].serviceStatus)
        } catch (exception:java.lang.IndexOutOfBoundsException) {
            Log.e("Error", exception.toString())
        }
        return convertView
    }

    private fun flag(position:Int):Boolean {
        try {
            return data[position].serviceStatus != ServiceInfo.SERVICE_HEADER_AVAILABLE && data[position].serviceStatus != ServiceInfo.SERVICE_HEADER_INSTALLED
        } catch(exception:IndexOutOfBoundsException) {
            Log.e("Error", exception.toString())
        }
        return false
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                data = results.values as ArrayList<ServiceInfo>
                notifyDataSetChanged()
            }
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()
                val FilteredList: ArrayList<ServiceInfo> = ArrayList()
                if (constraint == null || constraint.isEmpty()) {
                    results.values = dataIn; results.count = dataIn.size
                } else {
                    for (i in 0 until dataIn.size) {
                        val data: ServiceInfo = dataIn[i]
                        if (data.name.toLowerCase().contains(constraint.toString().toLowerCase()) && data.name.toLowerCase() != "Installed" && data.name.toLowerCase() != "Available") FilteredList.add(data)
                    }
                    results.values = FilteredList; results.count = FilteredList.size
                }
                return results
            }
        }
    }

}