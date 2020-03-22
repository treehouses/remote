package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.util.ArrayList;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.ServicesListAdapter;
import io.treehouses.remote.bases.BaseServicesFragment;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesDetailsFragment extends BaseServicesFragment {

    View view;
    private Spinner serviceSelector;
    private ImageView logo;
    private ProgressBar progressBar;
    private TextView serviceInfo;

    private boolean received = false;
    private boolean infoClicked;
    private int quoteCount;
    private String buildString;
    private int[] versionIntNumber;

    private boolean SVGSent = false;
    private String buildSVG = "";

    private ServicesListAdapter spinnerAdapter;
    private ArrayList<ServiceInfo> services;


    public ServicesDetailsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mChatService = listener.getChatService();
//        mChatService.updateHandler(mHandler);

        view = inflater.inflate(R.layout.activity_services_details, container, false);
        logo = view.findViewById(R.id.service_logo);
        serviceSelector = view.findViewById(R.id.pickService);
        progressBar = view.findViewById(R.id.progressBar);
        serviceInfo = view.findViewById(R.id.service_info);
        services = new ArrayList<>();
        services.add(new ServiceInfo("planet", ServiceInfo.SERVICE_RUNNING));
        spinnerAdapter = new ServicesListAdapter(getContext(), services);
        serviceSelector.setAdapter(spinnerAdapter);

        return view;
    }

    public final Handler handlerDetails = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    String output = (String) msg.obj;
                    moreActions(output);
                    break;
                case Constants.MESSAGE_WRITE:
                    String write_msg = new String((byte[]) msg.obj);
                    Log.d("WRITE", write_msg);
                    break;

            }
        }
    };

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

    private void onClickInfo(ServiceInfo selected) {
        progressBar.setVisibility(View.VISIBLE);
        writeToRPI("treehouses services " + selected.name + " info");
        quoteCount = 0;
        buildString = "";
        infoClicked = true;

    }

    private void increaseQuoteCount(String output) {
        quoteCount += getQuoteCount(output);
        buildString += output;
        if (output.startsWith("https://")) {
            buildString += "\n\n";
        }
        if (quoteCount >= 2) {
            serviceInfo.setText(buildString);
            progressBar.setVisibility(View.GONE);
        }
    }
    private boolean isLocalUrl(String output) {
        return output.contains(".") && output.contains(":") && output.length() < 20 && !received;
    }

    private void moreActions(String output) {
        if (SVGSent) {
            buildSVG += output;
            if (output.contains("</svg>")) {
                SVGSent = false;
                showIcon(buildSVG);
                onClickInfo(((ServiceInfo) serviceSelector.getSelectedItem()));
                buildSVG = "";
            }
        }
        else if (isLocalUrl(output)) {
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
            serviceInfo.setText(output);
        }
        else if (output.startsWith("Available: ")) {
            buildSVG = "";
            writeToRPI("treehouses services " + ((ServiceInfo) serviceSelector.getSelectedItem()).name + " icon\n");
        }
        else if (output.contains("xml")) {
            SVGSent = true;
        }

    }

    private void showIcon(String s) {
        try {
            SVG svg = SVG.getFromString(s);
            PictureDrawable pd = new PictureDrawable(svg.renderToPicture());

            logo.setImageDrawable(pd);
        } catch (SVGParseException e) {
            e.printStackTrace();
        }
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

}












