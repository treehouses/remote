package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import java.util.ArrayList;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.ServicesListAdapter;
import io.treehouses.remote.bases.BaseServicesFragment;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesTabFragment extends BaseServicesFragment implements AdapterView.OnItemClickListener {

    private View view;
    private ProgressBar progressBar;
    private ArrayList<ServiceInfo> services;
    ServicesListAdapter adapter;
    private TextView tvMessage;
    private boolean received = false;
    private boolean infoClicked;
    private int quoteCount;
    private String buildString;
    private int[] versionIntNumber;


    public ServicesTabFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);

        writeToRPI("treehouses version\n");

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
        } else if (output.contains("Available:")) {
            //Read
            tvMessage.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
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

        else {
            moreActions(output);
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

    private void increaseQuoteCount(String output) {
        quoteCount += getQuoteCount(output);
        buildString += output;
        if (output.startsWith("https://")) {
            buildString += "\n\n";
        }
        if (quoteCount >= 2) {
            showInfoDialog(buildString);
            progressBar.setVisibility(View.GONE);
        }
    }

    private boolean isVersionNumber(String s) {
        if (!s.contains(".")) return false;
        String[] parts = s.split("[.]");
        int[] intParts = new int[3];
        if (parts.length != 3) return false;
        for (int i = 0; i < parts.length; i++) {
            try {
                intParts[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        versionIntNumber = intParts;
        return true;
    }

    private boolean isLocalUrl(String output) {
        return output.contains(".") && output.contains(":") && output.length() < 20 && !received;
    }

    private void moreActions(String output) {
        if (isLocalUrl(output)) {
            received = true;
            openLocalURL(output);
            progressBar.setVisibility(View.GONE);
        }
        else if (output.contains(".onion") && ! received) {
            received = true;
            openTorURL(output);
            progressBar.setVisibility(View.GONE);
        }
        else if (infoClicked) {
            increaseQuoteCount(output);
        }
        else if (isVersionNumber(output)) {
            writeToRPI("treehouses remote services available\n");
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

    private void onClickInstall(ServiceInfo selected) {
        if (selected.serviceStatus == ServiceInfo.SERVICE_AVAILABLE && checkVersion(versionIntNumber)) {
            performService("Installing", "treehouses services " + selected.name + " install\n", selected.name);
            writeToRPI("treehouses remote services available\n");
        }
        else if (selected.serviceStatus == ServiceInfo.SERVICE_AVAILABLE && !checkVersion(versionIntNumber)) {
            performService("Installing", "treehouses services " + selected.name + " up\n", selected.name);
            writeToRPI("treehouses remote services available\n");
        }
        else if (installedOrRunning(selected)) {
            showDeleteDialog(selected);
        }
    }

    private void onClickStart(ServiceInfo selected) {
        if (selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED && checkVersion(versionIntNumber)) {
            performService("Starting", "treehouses services " + selected.name + " up\n", selected.name);
        }
        else if (selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED && !checkVersion(versionIntNumber)) {
            performService("Starting", "treehouses services " + selected.name + " start\n", selected.name);
        }
        else if (selected.serviceStatus == ServiceInfo.SERVICE_RUNNING) {
            performService("Stopping", "treehouses services " + selected.name + " stop\n", selected.name);
        }
    }

    private void onClickRestart(ServiceInfo selected) {
        if (selected.serviceStatus != ServiceInfo.SERVICE_AVAILABLE) performService("Restarting", "treehouses services " + selected.name + " restart\n", selected.name);

    }

    private void onClickLink(ServiceInfo selected) {
        //reqUrls();
        View view = getLayoutInflater().inflate(R.layout.dialog_choose_url, null);
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle("Select URL type")
                .create();

        setOnClick(view, R.id.local_button, "treehouses services " + selected.name + " url local \n", alertDialog);
        setOnClick(view, R.id.tor_button, "treehouses services " + selected.name + " url tor \n", alertDialog);

        alertDialog.show();
        received = false;
    }

    private void setOnClick(View v, int id, String command, AlertDialog alertDialog) {
        v.findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToRPI(command);
                alertDialog.dismiss();
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }


    private void onClickInfo(ServiceInfo selected) {
        progressBar.setVisibility(View.VISIBLE);
        writeToRPI("treehouses services " + selected.name + " info");
        infoClicked = true;
        quoteCount = 0;
        buildString = "";
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ServiceInfo selected = services.get(position);
        infoClicked = false;
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

            case R.id.service_info:
                onClickInfo(selected);
                break;

            case R.id.link_button:
                onClickLink(selected);
                break;
        }
    }


}