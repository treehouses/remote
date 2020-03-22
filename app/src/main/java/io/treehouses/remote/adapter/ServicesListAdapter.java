package io.treehouses.remote.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mikepenz.materialize.color.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import io.treehouses.remote.R;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesListAdapter extends ArrayAdapter<ServiceInfo> {
    private ArrayList<ServiceInfo> data;
    private Context context;
    private TextView name;
    private ImageView status;
    //private Button start, install, restart, link, info;

    public ServicesListAdapter(Context context, ArrayList<ServiceInfo> services) {
        super(context,0,services);
        this.data = services;
        this.context = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (data.get(position).serviceStatus != ServiceInfo.SERVICE_HEADER) {
            convertView = LayoutInflater.from(context).inflate(R.layout.services_row_layout, parent, false);
        }
        else if (data.get(position).serviceStatus == ServiceInfo.SERVICE_HEADER) {
            convertView = LayoutInflater.from(context).inflate(R.layout.services_section_header, parent, false);
        }
        findViews(convertView);



        name.setText(data.get(position).name);

        setStatus(data.get(position).serviceStatus);

//        setOnClick(parent, convertView, R.id.start_service, position);
//        setOnClick(parent, convertView, R.id.install_service, position);
//        setOnClick(parent, convertView, R.id.restart_service, position);
//        setOnClick(parent, convertView, R.id.link_button, position);
//        setOnClick(parent, convertView, R.id.service_info, position);

        return convertView;
    }

    private void findViews(View view) {
        name = view.findViewById(R.id.service_name);
        status = view.findViewById(R.id.service_status);
//        start = view.findViewById(R.id.start_service);
//        install = view.findViewById(R.id.install_service);
//        restart = view.findViewById(R.id.restart_service);
//        link = view.findViewById(R.id.link_button);
//        info = view.findViewById(R.id.service_info);
    }

    private void setStatus(int statusCode) {
        if (statusCode == ServiceInfo.SERVICE_AVAILABLE) {

            setButtons(false, false, false);

            name.setTextColor(context.getResources().getColor(R.color.md_grey_600));
            status.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_red));
        } else if (statusCode == ServiceInfo.SERVICE_INSTALLED) {

            setButtons(false, true, false);

            name.setTextColor(context.getResources().getColor(R.color.md_grey_600));
            status.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_yellow));

        } else if (statusCode == ServiceInfo.SERVICE_RUNNING) {
            setButtons(true, true, true);

            name.setTextColor(context.getResources().getColor(R.color.md_green_500));
            status.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_green));

        }
    }

    private void setButtons(boolean one, boolean two, boolean three) {
//        setStart(one);
//        setInstall(two);
//        restart.setEnabled(three);
//
//        if (one) {
//            link.setVisibility(View.VISIBLE);
//        } else {
//            link.setVisibility(View.GONE);
//        }
    }

//    private void setStart(boolean started) {
//        if (started) start.setText("Stop");
//        else start.setText("Start");
//    }
//
//    private void setInstall(boolean installed) {
//        if (installed) {
//            install.setText("Uninstall");
//            start.setEnabled(true);
//        } else {
//            install.setText("Install");
//            start.setEnabled(false);
//        }
//    }

    private void setOnClick(ViewGroup parent, View convertView, int id, int position) {
        convertView.findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0);
            }
        });
    }
}
