package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.util.ArrayList;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.ServicesListAdapter;
import io.treehouses.remote.bases.BaseServicesFragment;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesDetailsFragment extends BaseServicesFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {

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

    private Button install, start, openLink;

    private ServicesListAdapter spinnerAdapter;
    private ArrayList<ServiceInfo> services;

    private ServiceInfo selected;

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
        serviceInfo.setMovementMethod(LinkMovementMethod.getInstance());
        serviceInfo.setFocusable(true);
        services = new ArrayList<>();
        spinnerAdapter = new ServicesListAdapter(getContext(), services, getResources().getColor(R.color.md_grey_600));
        serviceSelector.setAdapter(spinnerAdapter);
        serviceSelector.setOnItemSelectedListener(this);
        versionIntNumber = new int[3];

        install = view.findViewById(R.id.install_button);
        start = view.findViewById(R.id.start_button);
        openLink = view.findViewById(R.id.openLink);

        install.setOnClickListener(this);
        start.setOnClickListener(this);
        openLink.setOnClickListener(this);
        return view;
    }

    public final Handler handlerDetails = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    String output = (String) msg.obj;
                    int a = performAction(output, serviceInfo, progressBar, services, versionIntNumber, spinnerAdapter);
                    if (a == -1) moreActions(output);
                    //Services Running has been updated
                    else if (a == 4 && spinnerAdapter.getCount() > 0) resetServices();
                    break;

                case Constants.MESSAGE_WRITE:
                    String write_msg = new String((byte[]) msg.obj);
                    Log.d("WRITE", write_msg);
                    break;
            }
        }
    };

    private void resetServices() {
        if ((ServiceInfo) serviceSelector.getSelectedItem() == null) {
            serviceSelector.setSelection(inServiceList("planet", services));
        }
        else if (selected != null){
            serviceSelector.setSelection(spinnerAdapter.getPosition(selected));
        }
        updateButtons(((ServiceInfo) serviceSelector.getSelectedItem()).serviceStatus);
        buildSVG = "";

        loadInfo(((ServiceInfo) serviceSelector.getSelectedItem()));

    }

    private void moreActions(String output) {
        if (containsXML(output)) {
            SVGSent = true;
            buildSVG += output;
        }
        else if (SVGSent) {
            buildingSVG(output);
        }
        else if (isLocalUrl(output)) {
            received = true;
            openLocalURL(output.trim());
            progressBar.setVisibility(View.GONE);
        }

        else if (infoClicked) {
            increaseQuoteCount(output);
        }
        else if (output.contains(".onion") && !received) {
            received = true;
            openTorURL(output.trim());
            progressBar.setVisibility(View.GONE);
        }

    }

    private void buildingSVG(String output) {
        buildSVG += output;
        if (output.contains("</svg>")) {
            SVGSent = false;
            showIcon(buildSVG);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void loadInfo(ServiceInfo selected) {
        progressBar.setVisibility(View.VISIBLE);
        writeToRPI("treehouses services " + selected.name + " info\n");
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
            SpannableString s = new SpannableString(buildString);
            Linkify.addLinks(s, Linkify.ALL);
            serviceInfo.setText(s);
            writeToRPI("treehouses services " + ((ServiceInfo) serviceSelector.getSelectedItem()).name + " icon\n");
        }
    }
    private boolean isLocalUrl(String output) {
        return output.contains(".") && output.contains(":") && output.length() < 25 && !received;
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
    private void onClickInstall(ServiceInfo selected) {
        if (selected.serviceStatus == ServiceInfo.SERVICE_AVAILABLE && checkVersion(versionIntNumber)) {
            performService("Installing", "treehouses services " + selected.name + " install\n", selected.name);
            writeToRPI("treehouses remote services available\n");
        }
        else if (selected.serviceStatus == ServiceInfo.SERVICE_AVAILABLE && !checkVersion(versionIntNumber)) {
            performService("Installing", "treehouses services " + selected.name + " up\n", selected.name);
            writeToRPI("treehouses remote services available\n");
        }
        else if (installedOrRunning(selected)) showDeleteDialog(selected);
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
        writeToRPI("treehouses remote services available\n");
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

    private void setButtons(boolean started, boolean installed, boolean three) {
        if (started) {
            start.setText("Stop");
            openLink.setVisibility(View.VISIBLE);
        }
        else {
            start.setText("Start");
            openLink.setVisibility(View.GONE);
        }
        if (installed) {
            install.setText("Uninstall");
            start.setEnabled(true);
        } else {
            install.setText("Install");
            start.setEnabled(false);
        }
        //restart.setEnabled(three);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int statusCode = services.get(position).serviceStatus;
        if (statusCode == ServiceInfo.SERVICE_HEADER_AVAILABLE || statusCode == ServiceInfo.SERVICE_HEADER_INSTALLED) return;
        buildSVG = "";
        serviceSelector.setSelection(inServiceList(services.get(position).name, services));
        loadInfo(((ServiceInfo) serviceSelector.getSelectedItem()));

        updateButtons(statusCode);
    }

    private void updateButtons(int statusCode) {
        switch (statusCode) {
            case ServiceInfo.SERVICE_AVAILABLE:
                setButtons(false, false, false);
                break;
            case ServiceInfo.SERVICE_INSTALLED:
                setButtons(false, true, false);
                break;
            case ServiceInfo.SERVICE_RUNNING:
                setButtons(true, true, true);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    @Override
    public void onClick(View v) {
        ServiceInfo serviceInfo = (ServiceInfo) serviceSelector.getSelectedItem();
        selected = serviceInfo;
        switch (v.getId()) {
            case R.id.install_button:
                onClickInstall(serviceInfo);
                break;
            case R.id.start_button:
                onClickStart(serviceInfo);
                break;
            case R.id.openLink:
                onClickLink(serviceInfo);
                break;

        }
    }
}

