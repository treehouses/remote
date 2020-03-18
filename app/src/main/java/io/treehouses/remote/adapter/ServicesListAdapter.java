package io.treehouses.remote.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import io.treehouses.remote.R;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesListAdapter extends BaseAdapter {
    private HashMap<String, ArrayList<ServiceInfo>> data;
    private ArrayList<String> sectionHeaders;
    private Context context;
    private TextView name;
    private ImageView status;
    //private Button start, install, restart, link, info;

    public ServicesListAdapter(Context context, HashMap<String, ArrayList<ServiceInfo>> services) {
        this.data = services;
        this.context = context;
        sectionHeaders = new ArrayList<>();
        sectionHeaders.addAll(services.keySet());
    }


    @Override
    public int getCount() {
        int count = 0;
        for (int i = 0; i < sectionHeaders.size(); i++) {
            count++;
            count += data.get(sectionHeaders.get(i)).size();
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.services_row_layout, parent, false);
        }
        findViews(convertView);

        name.setText(data.get(sectionHeaders.get(getSectionPosition(position))).get(position).name);

        setStatus(data.get(sectionHeaders.get(getSectionPosition(position))).get(position).serviceStatus);

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
            status.setImageDrawable(context.getResources().getDrawable(R.drawable.circle));

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
    private int getRelativePosition (int sectionPosition, int absolutePosition) {

        return 0;
    }
    private int getSectionPosition(int position) {
        int count = 0;
        for (int i = 0 ; i < sectionHeaders.size(); i++) {
            count += data.get(sectionHeaders.get(i)).size() + 1;
            if (position < count) {
                return i;
            }
        }
        return sectionHeaders.size()-1;
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
