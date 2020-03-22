package io.treehouses.remote.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.ServicesListAdapter;
import io.treehouses.remote.bases.BaseServicesFragment;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesTabFragment extends BaseServicesFragment implements AdapterView.OnItemClickListener {

    private View view;
    private ProgressBar progressBar;
    public ArrayList<ServiceInfo> services;
    ServicesListAdapter adapter;
    private TextView tvMessage;


    public ServicesTabFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mChatService = listener.getChatService();
//        mChatService.updateHandler(handlerOverview);

        view = inflater.inflate(R.layout.activity_services_tab_fragment, container, false);
        progressBar = view.findViewById(R.id.progress_services);
        tvMessage = view.findViewById(R.id.tv_message);
        progressBar.setVisibility(View.VISIBLE);
        services = new ArrayList<ServiceInfo>();

        ListView listView = view.findViewById(R.id.listView);
        adapter = new ServicesListAdapter(getActivity(), services);
        listView.setAdapter(adapter);

        listView.setItemsCanFocus(false);

        listView.setOnItemClickListener(this);

        return view;
    }

    private void performAction(String output) {
        if (output.startsWith("Usage:")) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Feature not available please upgrade cli version.");
            progressBar.setVisibility(View.GONE);
        }
        else if (output.contains("Available:")) {
            //Read
            tvMessage.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            updateServiceList(output.substring(output.indexOf(":") + 2).split(" "), ServiceInfo.SERVICE_AVAILABLE);
            writeToRPI("treehouses remote services installed\n");
        }
        else if (output.contains("Installed:")) {
            updateServiceList(output.substring(output.indexOf(":") + 2).split(" "), ServiceInfo.SERVICE_INSTALLED);
            writeToRPI("treehouses remote services running\n");
        }
        else if (output.contains("Running:")) {
            updateServiceList(output.substring(output.indexOf(":") + 2).split(" "), ServiceInfo.SERVICE_RUNNING);
        }

    }


    public final Handler handlerOverview = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    String output = (String) msg.obj;
                    performAction(output);
                    break;
                case Constants.MESSAGE_WRITE:
                    String write_msg = new String((byte[]) msg.obj);
                    Log.d("WRITE", write_msg);
                    break;

            }
        }
    };

    private void updateServiceList(String[] stringList, int identifier) {
        for (String name : stringList) {
            int a = inServiceList(name);
            if (a >= 0 && services.get(a).serviceStatus != ServiceInfo.SERVICE_HEADER) services.get(a).serviceStatus = identifier;
            else if (name.trim().length() > 0) services.add(new ServiceInfo(name, identifier));
        }

        if (identifier == ServiceInfo.SERVICE_RUNNING) {
            Collections.sort(services);
            if (inServiceList("Installed") == -1) services.add(0, new ServiceInfo("Installed", ServiceInfo.SERVICE_HEADER));
            int i = 0;
            while (i < services.size() && (services.get(i).serviceStatus != ServiceInfo.SERVICE_AVAILABLE)) {
                Log.d("i", services.get(i).name + " "+i);
                i++;
            }
            if (inServiceList("Available") == -1) services.add(i, new ServiceInfo("Available",ServiceInfo.SERVICE_HEADER));
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);

        }
    }

    private int inServiceList(String name) {
        for (int i = 0; i < services.size(); i++) {
            if (services.get(i).name.equals(name)) return i;
        }
        return -1;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ServiceInfo selected = services.get(position);
//        switch (view.getId()) {
//            case R.id.start_service:
//                onClickStart(selected);
//                writeToRPI("treehouses remote services available\n");
//                break;
//            case R.id.install_service:
//                onClickInstall(selected);
//                break;
//
//            case R.id.restart_service:
//                onClickRestart(selected);
//                writeToRPI("treehouses remote services available\n");
//                break;
//
//            case R.id.service_info:
//                onClickInfo(selected);
//                break;
//
//            case R.id.link_button:
//                onClickLink(selected);
//                break;
//        }
    }


}