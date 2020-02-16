package io.treehouses.remote.Fragments;

import android.content.Intent;
import android.net.Uri;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.ServicesListAdapter;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesTabFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private View view;
    private ProgressBar progressBar;
    private ArrayList<ServiceInfo> services;
    ServicesListAdapter adapter;
    private TextView tvMessage;
    private String service_name = "";
    private boolean received = false;

    public ServicesTabFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);

        writeToRPI("treehouses remote services available\n");

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

    private void writeToRPI(String ping) {
        mChatService.write(ping.getBytes());
    }

    private void performAction(String output) {
        if (output.startsWith("Usage:")) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Feature not available please upgrade cli version.");
            progressBar.setVisibility(View.GONE);
        } else if (output.contains("Available:")) {
            //Read
            tvMessage.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            updateServiceList(output.substring(output.indexOf(":") + 2).split(" "), ServiceInfo.SERVICE_AVAILABLE);
            writeToRPI("treehouses remote services installed\n");
        }
        else if (output.contains(".") && output.contains(":") && output.length() < 20 && !received) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + output));
            startActivity(intent);
            received = true;
        }
        else{
            checkServiceInfo(output);
        }
    }

    private final Handler mHandler = new Handler() {
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

    private void checkServiceInfo(String output) {
        if (output.contains("Installed:")) {
            updateServiceList(output.substring(output.indexOf(":") + 2).split(" "), ServiceInfo.SERVICE_INSTALLED);
            writeToRPI("treehouses remote services running\n");
        } else if (output.contains("Running:")) {
            updateServiceList(output.substring(output.indexOf(":") + 2).split(" "), ServiceInfo.SERVICE_RUNNING);
        }
    }

    private void updateServiceList(String[] stringList, int identifier) {
        for (String name : stringList) {
            int a = inServiceList(name);
            if (a >= 0) services.get(a).serviceStatus = identifier;
            else if (name.trim().length() > 0) services.add(new ServiceInfo(name, identifier));
        }
        adapter.notifyDataSetChanged();
    }

    private int inServiceList(String name) {
        for (int i = 0; i < services.size(); i++) {
            if (services.get(i).name.equals(name)) return i;
        }
        return -1;
    }

    private void performService(String action, String command, String name) {
        Log.d("SERVICES", action + " " + name);
        Toast.makeText(getContext(), name + " " + action, Toast.LENGTH_LONG).show();
        writeToRPI(command);
    }

    private void onClickInstall(ServiceInfo selected) {
        if (selected.serviceStatus == ServiceInfo.SERVICE_AVAILABLE) {
            performService("Installing", "treehouses services " + selected.name + " up\n", selected.name);
            writeToRPI("treehouses remote services available\n");
        }
        else if (selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED || selected.serviceStatus == ServiceInfo.SERVICE_RUNNING) {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Delete " + selected.name + "?")
                    .setMessage("Are you sure you would like to delete this service? All of its data will be lost and the service must be reinstalled.")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            performService("Uninstalling", "treehouses services " + selected.name + " down\n", selected.name);
                            writeToRPI("treehouses remote services available\n");
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            alertDialog.show();
        }
    }

    private void onClickStart(ServiceInfo selected) {
        if (selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED) {
            performService("Starting", "treehouses services " + selected.name + " start\n", selected.name);
        }
        else if (selected.serviceStatus == ServiceInfo.SERVICE_RUNNING) {
            performService("Stopping", "treehouses services " + selected.name + " stop\n", selected.name);
        }
    }

    private void onClickRestart(ServiceInfo selected) {
        if (selected.serviceStatus != ServiceInfo.SERVICE_AVAILABLE) {
            performService("Restarting", "treehouses services " + selected.name + " restart\n", selected.name);
        }
    }

    private void onClickLink(ServiceInfo selected) {
        //reqUrls();
        writeToRPI("treehouses services " + selected.name + " url local \n");
        received = false;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ServiceInfo selected = services.get(position);
        switch (view.getId()) {
            case R.id.start_service:
                onClickStart(selected);
                writeToRPI("treehouses remote services available\n");
                break;
            case R.id.install_service:
                onClickInstall(selected);
                break;

            case R.id.restart_service:
                onClickRestart(selected);
                writeToRPI("treehouses remote services available\n");
                break;

            case R.id.link_button:
                onClickLink(selected);
                break;
        }
    }


}

