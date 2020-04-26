package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import io.treehouses.remote.R;
import io.treehouses.remote.callback.ServiceAction;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServiceCardFragment extends Fragment implements View.OnClickListener {
    private ServiceInfo serviceData;
    private ImageView logo;
    private TextView serviceInfo;
    private Button install, start, openLink;
    private ServiceAction actionListener;
    private CheckBox autorunCheck;

    public ServiceCardFragment(ServiceInfo serviceData) {
        this.serviceData = serviceData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.service_card, container, false);

        logo = view.findViewById(R.id.service_logo);
        serviceInfo = view.findViewById(R.id.service_info);
        serviceInfo.setMovementMethod(LinkMovementMethod.getInstance());
        serviceInfo.setFocusable(true);

        install = view.findViewById(R.id.install_button);
        start = view.findViewById(R.id.start_button);
        openLink = view.findViewById(R.id.openLink);
        autorunCheck = view.findViewById(R.id.autorun_checked);

        if (!serviceData.isHeader()) {
            setServiceInfo(serviceData.info);
            showIcon(serviceData.icon);
            updateButtons(serviceData.serviceStatus);
            setAutorun(serviceData.autorun);

            install.setOnClickListener(this);
            start.setOnClickListener(this);
            openLink.setOnClickListener(this);
            autorunCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    actionListener.onClickAutorun(serviceData, isChecked);
                }
            });
        }
        return view;
    }

    private void setAutorun(String autorun) {
        if (autorun.contains("true")) {
            autorunCheck.setChecked(true);
        } else {
            autorunCheck.setChecked(false);
        }
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
            start.setVisibility(View.VISIBLE);
            autorunCheck.setVisibility(View.VISIBLE);
        } else {
            install.setText("Install");
            start.setVisibility(View.GONE);
            autorunCheck.setVisibility(View.GONE);
        }
        //restart.setEnabled(three);
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

    private void showIcon(String s) {
        try {
            Log.d(serviceData.name, "showIcon:" + serviceData.icon);
            SVG svg = SVG.getFromString(s);
            PictureDrawable pd = new PictureDrawable(svg.renderToPicture());
            logo.setImageDrawable(pd);
        } catch (SVGParseException e) {
            e.printStackTrace();
        }
    }

    private void setServiceInfo(String s) {
        SpannableString spannableString = new SpannableString(s);
        Linkify.addLinks(spannableString, Linkify.ALL);
        serviceInfo.setText(s);
        serviceInfo.setMovementMethod(LinkMovementMethod.getInstance());
    }

//    private void onClickRestart(ServiceInfo selected) {
//        if (selected.serviceStatus != ServiceInfo.SERVICE_AVAILABLE) performService("Restarting", "treehouses services " + selected.name + " restart\n", selected.name);
//
//    }
//    private void setOnClick(View v, int id, String command, AlertDialog alertDialog) {
//        v.findViewById(id).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                writeToRPI(command);
//                alertDialog.dismiss();
////                progressBar.setVisibility(View.VISIBLE);
//            }
//        });
//    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.install_button:
                actionListener.onClickInstall(serviceData);
                break;
            case R.id.start_button:
                actionListener.onClickStart(serviceData);
                break;
            case R.id.openLink:
                actionListener.onClickLink(serviceData);
                break;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        actionListener = (ServiceAction) getParentFragment();
    }

}
