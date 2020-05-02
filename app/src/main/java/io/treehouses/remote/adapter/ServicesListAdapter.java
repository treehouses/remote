package io.treehouses.remote.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import io.treehouses.remote.R;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesListAdapter extends ArrayAdapter<ServiceInfo> {
    private ArrayList<ServiceInfo> data;
    private Context context;
    private TextView name;
    private ImageView status;
    private int headerColour;
    //private Button start, install, restart, link, info;

    public ServicesListAdapter(Context context, ArrayList<ServiceInfo> services, int headerColour) {
        super(context,0,services);
        this.data = services;
        this.context = context;
        this.headerColour = headerColour;
    }

    @Override
    public ServiceInfo getItem(int position) {
        return data.get(position);
    }

    @Override
    public int getPosition(ServiceInfo s) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).name.equals(s.name) && data.get(i).serviceStatus == s.serviceStatus) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(data);
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public boolean isEnabled(int position){
        return data.get(position).serviceStatus != ServiceInfo.SERVICE_HEADER_INSTALLED && data.get(position).serviceStatus != ServiceInfo.SERVICE_HEADER_AVAILABLE;
    }


    private void findViews(View view) {
        name = view.findViewById(R.id.service_name);
        status = view.findViewById(R.id.service_status);
        name.setTextColor(headerColour);
//        start = view.findViewById(R.id.start_service);
//        install = view.findViewById(R.id.install_service);
//        restart = view.findViewById(R.id.restart_service);
//        link = view.findViewById(R.id.link_button);
//        info = view.findViewById(R.id.service_info);
    }

    private void setStatus(int statusCode) {
        if (statusCode == ServiceInfo.SERVICE_AVAILABLE) {

            //setButtons(false, false, false);

            name.setTextColor(context.getResources().getColor(R.color.md_grey_600));
            status.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_red));
        } else if (statusCode == ServiceInfo.SERVICE_INSTALLED) {

            //setButtons(false, true, false);

            name.setTextColor(context.getResources().getColor(R.color.md_grey_600));
            status.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_yellow));

        } else if (statusCode == ServiceInfo.SERVICE_RUNNING) {
            //setButtons(true, true, true);

            name.setTextColor(context.getResources().getColor(R.color.md_green_500));
            status.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_green));

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

    private View initView(int position, View convertView, ViewGroup parent) {
        if (data.get(position).serviceStatus != ServiceInfo.SERVICE_HEADER_AVAILABLE && data.get(position).serviceStatus != ServiceInfo.SERVICE_HEADER_INSTALLED) {
            convertView = LayoutInflater.from(context).inflate(R.layout.services_row_layout, parent, false);
        }
        else {
            convertView = LayoutInflater.from(context).inflate(R.layout.services_section_header, parent, false);

        }
        findViews(convertView);



        name.setText(data.get(position).name);

        setStatus(data.get(position).serviceStatus);

        return convertView;
    }
}
