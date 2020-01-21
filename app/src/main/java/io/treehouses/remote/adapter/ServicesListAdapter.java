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

public class ServicesListAdapter extends ArrayAdapter<String> {
    private ArrayList<String> data;
    private Context context;
    public ServicesListAdapter(Context context, ArrayList<String> services) {
        super(context, 0, services);
        this.data = services;
        this.context = context;
    };
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.services_row_layout, parent, false);
        }

        TextView name = convertView.findViewById(R.id.service_name);
        name.setText(data.get(position));

        setOnClick(parent, convertView, R.id.start_service, position);
        setOnClick(parent, convertView, R.id.stop_service, position);
        setOnClick(parent, convertView, R.id.install_service, position);
        setOnClick(parent, convertView, R.id.uninstall_service, position);

        return convertView;
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
