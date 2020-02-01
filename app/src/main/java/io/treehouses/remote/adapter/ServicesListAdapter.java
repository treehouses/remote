package io.treehouses.remote.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import io.treehouses.remote.R;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesListAdapter extends ArrayAdapter<ServiceInfo> {
    private ArrayList<ServiceInfo> data;
    private Context context;
    private TextView name;
    private Button start, install, restart;
    public ServicesListAdapter(Context context, ArrayList<ServiceInfo> services) {
        super(context, 0, services);
        this.data = services;
        this.context = context;
    };
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.services_row_layout, parent, false);
        }
        findViews(convertView);

        name.setText(data.get(position).name);

        setStatus(data.get(position).serviceStatus);

        setOnClick(parent, convertView, R.id.start_service, position);
        setOnClick(parent, convertView, R.id.install_service, position);
        setOnClick(parent, convertView, R.id.restart_service, position);

        return convertView;
    }

    private void findViews(View view) {
        name = view.findViewById(R.id.service_name);
        start = view.findViewById(R.id.start_service);
        install = view.findViewById(R.id.install_service);
        restart = view.findViewById(R.id.restart_service);
    }

    private void setStatus(int status) {
        if (status == ServiceInfo.SERVICE_AVAILABLE) {
            setStart(false);
            setInstall(false);

            name.setTextColor(context.getResources().getColor(R.color.md_grey_600));
        }
        else if (status == ServiceInfo.SERVICE_INSTALLED) {
            setStart(false);
            setInstall(true);

            name.setTextColor(context.getResources().getColor(R.color.md_grey_600));
        }
        else if (status == ServiceInfo.SERVICE_RUNNING) {
            setStart(true);
            setInstall(true);

            name.setTextColor(context.getResources().getColor(R.color.md_green_500));
        }
    }

    private void setStart(boolean value) {
        if (value) {
            start.setText("Stop");
            restart.setEnabled(true);
        }
        else {
            start.setText("Start");
            restart.setEnabled(false);
        }
    }

    private void setInstall(boolean installed) {
        if (installed) {
            install.setText("Uninstall");
            start.setEnabled(true);
        }
        else {
            install.setText("Install");
            start.setEnabled(false);
        }
    }

    private void setOnClick(ViewGroup parent, View convertView, int id, int position) {
        convertView.findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0);
            }
        });
    }
}
