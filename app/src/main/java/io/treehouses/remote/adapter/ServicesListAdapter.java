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
    private Button start, stop, install, uninstall;
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
        setOnClick(parent, convertView, R.id.stop_service, position);
        setOnClick(parent, convertView, R.id.install_service, position);
        setOnClick(parent, convertView, R.id.uninstall_service, position);

        return convertView;
    }

    private void findViews(View view) {
        name = view.findViewById(R.id.service_name);
        start = view.findViewById(R.id.start_service);
        stop = view.findViewById(R.id.stop_service);
        install = view.findViewById(R.id.install_service);
        uninstall = view.findViewById(R.id.uninstall_service);

    }

    private void setStatus(int status) {
        if (status == ServiceInfo.SERVICE_AVAILABLE) {
            setButtons(false, false, true, false);

            name.setTextColor(context.getResources().getColor(R.color.md_grey_600));
        }
        else if (status == ServiceInfo.SERVICE_INSTALLED) {
            setButtons(true, false, false, true);

            name.setTextColor(context.getResources().getColor(R.color.md_grey_600));
        }
        else if (status == ServiceInfo.SERVICE_RUNNING) {
            setButtons(false, true, false, true);

            name.setTextColor(context.getResources().getColor(R.color.md_green_500));
        }
    }

    private void setButtons(boolean first, boolean second, boolean third, boolean fourth) {
        start.setEnabled(first);
        stop.setEnabled(second);
        install.setEnabled(third);
        uninstall.setEnabled(fourth);
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
