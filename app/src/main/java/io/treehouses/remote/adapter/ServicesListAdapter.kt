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

class ServicesListAdapter(context: Context, services: ArrayList<ServiceInfo>, headerColour: Int) : ArrayAdapter<ServiceInfo>(context, 0, services) {
    private val data: ArrayList<ServiceInfo>
    private val context: Context
    private var name: TextView? = null
    private var status: ImageView? = null
    private val headerColour: Int
    override fun getItem(position: Int): ServiceInfo {
        return data[position]
    }

    override fun getPosition(s: ServiceInfo): Int {
        for (i in data.indices) {
            if (data[i].name.equals(s.name) && data[i].serviceStatus === s.serviceStatus) {
                return i
            }
        }
        return -1
    }

    override fun notifyDataSetChanged() {
        Collections.sort(data)
        super.notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun isEnabled(position: Int): Boolean {
        return data[position].serviceStatus !== ServiceInfo.SERVICE_HEADER_INSTALLED && data[position].serviceStatus !== ServiceInfo.SERVICE_HEADER_AVAILABLE
    }

    private fun findViews(view: View) {
        name = view.findViewById(R.id.service_name)
        status = view.findViewById(R.id.service_status)
        name.setTextColor(headerColour)
        //        start = view.findViewById(R.id.start_service);
//        install = view.findViewById(R.id.install_service);
//        restart = view.findViewById(R.id.restart_service);
//        link = view.findViewById(R.id.link_button);
//        info = view.findViewById(R.id.service_info);
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

    //    private void setOnClick(ViewGroup parent, View convertView, int id, int position) {
    //        convertView.findViewById(id).setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View v) {
    //                ((ListView) parent).performItemClick(v, position, 0);
    //            }
    //        });
    //    }
    private fun initView(position: Int, convertView: View, parent: ViewGroup): View {
        var convertView = convertView
        convertView = if (data[position].serviceStatus !== ServiceInfo.SERVICE_HEADER_AVAILABLE && data[position].serviceStatus !== ServiceInfo.SERVICE_HEADER_INSTALLED) {
            LayoutInflater.from(context).inflate(R.layout.services_row_layout, parent, false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.services_section_header, parent, false)
        }
        findViews(convertView)
        name.setText(data[position].name)
        setStatus(data[position].serviceStatus)
        return convertView
    }

    //private Button start, install, restart, link, info;
    init {
        data = services
        this.context = context
        this.headerColour = headerColour
    }
}