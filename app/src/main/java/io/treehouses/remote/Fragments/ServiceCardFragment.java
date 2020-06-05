package io.treehouses.remote.Fragments;

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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import io.treehouses.remote.callback.ServiceAction;
import io.treehouses.remote.databinding.ServiceCardBinding;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServiceCardFragment extends Fragment implements View.OnClickListener {
    private ServiceInfo serviceData;
    private ServiceAction actionListener;

    private ServiceCardBinding binding;

    public ServiceCardFragment(ServiceInfo serviceData) {
        this.serviceData = serviceData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ServiceCardBinding.inflate(inflater, container, false);

        binding.serviceInfo.setMovementMethod(LinkMovementMethod.getInstance());
        binding.serviceInfo.setFocusable(true);


        if (!serviceData.isHeader()) {
            setServiceInfo(serviceData.info);
            showIcon(serviceData.icon);
            updateButtons(serviceData.serviceStatus);
            setAutorun(serviceData.autorun);

            binding.installButton.setOnClickListener(this);
            binding.startButton.setOnClickListener(this);
            binding.openLink.setOnClickListener(this);
            binding.autorunChecked.setOnCheckedChangeListener((buttonView, isChecked) -> actionListener.onClickAutorun(serviceData, isChecked));
        }
        return binding.getRoot();
    }

    private void setAutorun(String autorun) {
        binding.autorunChecked.setChecked(autorun.contains("true"));
    }

    private void setButtons(boolean started, boolean installed, boolean three) {
        if (started) {
            binding.startButton.setText("Stop");
            binding.openLink.setVisibility(View.VISIBLE);
        }
        else {
            binding.startButton.setText("Start");
            binding.openLink.setVisibility(View.GONE);
        }
        if (installed) {
            binding.installButton.setText("Uninstall");
            binding.startButton.setVisibility(View.VISIBLE);
            binding.autorunChecked.setVisibility(View.VISIBLE);
        } else {
            binding.installButton.setText("Install");
            binding.startButton.setVisibility(View.GONE);
            binding.autorunChecked.setVisibility(View.GONE);
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
            binding.serviceLogo.setImageDrawable(pd);
        } catch (SVGParseException e) {
            e.printStackTrace();
        }
    }

    private void setServiceInfo(String s) {
        SpannableString spannableString = new SpannableString(s);
        Linkify.addLinks(spannableString, Linkify.ALL);
        binding.serviceInfo.setText(s);
        binding.serviceInfo.setMovementMethod(LinkMovementMethod.getInstance());
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
        if (binding.installButton.equals(v)) {
            actionListener.onClickInstall(serviceData);
        } else if (binding.startButton.equals(v)) {
            actionListener.onClickStart(serviceData);
        } else if (binding.openLink.equals(v)) {
            actionListener.onClickLink(serviceData);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        actionListener = (ServiceAction) getParentFragment();
    }

}
