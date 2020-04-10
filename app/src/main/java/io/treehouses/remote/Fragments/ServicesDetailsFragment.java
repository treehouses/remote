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

import androidx.viewpager.widget.ViewPager;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.util.ArrayList;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.ServiceCardAdapter;
import io.treehouses.remote.adapter.ServicesListAdapter;
import io.treehouses.remote.bases.BaseServicesFragment;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesDetailsFragment extends BaseServicesFragment implements AdapterView.OnItemSelectedListener, ViewPager.OnPageChangeListener {

    View view;
    private Spinner serviceSelector;
    private ProgressBar progressBar;

    private boolean received = false;

    private ServicesListAdapter spinnerAdapter;
    private ArrayList<ServiceInfo> services;

    private ServiceInfo selected;

    private ViewPager serviceCards;
    private ServiceCardAdapter serviceCardAdapter;

    private boolean scrolled = false;

    public ServicesDetailsFragment(ArrayList<ServiceInfo> serviceInfos){
        this.services = serviceInfos;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mChatService = listener.getChatService();
//        mChatService.updateHandler(mHandler);

        view = inflater.inflate(R.layout.activity_services_details, container, false);
        serviceSelector = view.findViewById(R.id.pickService);
        progressBar = view.findViewById(R.id.progressBar);

        spinnerAdapter = new ServicesListAdapter(getContext(), services, getResources().getColor(R.color.md_grey_600));
        serviceSelector.setAdapter(spinnerAdapter);
        serviceSelector.setSelection(1);
        serviceSelector.setOnItemSelectedListener(this);

        serviceCards = view.findViewById(R.id.services_cards);
        serviceCardAdapter = new ServiceCardAdapter(getChildFragmentManager(), services);
        serviceCards.setAdapter(serviceCardAdapter);

        serviceCards.addOnPageChangeListener(this);

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

    private void moreActions(String output) {
        if (isLocalUrl(output)) {
            received = true;
            openLocalURL(output.trim());
            progressBar.setVisibility(View.GONE);
        }
        else if (isTorURL(output, received)) {
            received = true;
            openTorURL(output.trim());
            progressBar.setVisibility(View.GONE);
        }

    }

    private boolean isLocalUrl(String output) {
        return output.contains(".") && output.contains(":") && output.length() < 25 && !received;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!scrolled) {
            int statusCode = services.get(position).serviceStatus;
            if (statusCode == ServiceInfo.SERVICE_HEADER_AVAILABLE || statusCode == ServiceInfo.SERVICE_HEADER_INSTALLED) return;

            int count = countHeadersBefore(position);
            serviceCards.setCurrentItem(position-count);
        }

//        serviceSelector.setSelection(inServiceList(services.get(position).name, services));
//        setServiceInfo(servicesData.getInfo().get(services.get(position).name));
//        showIcon(servicesData.getIcon().get(services.get(position).name));
//        updateButtons(statusCode);
    }



    @Override
    public void onNothingSelected(AdapterView<?> parent) { }



    public void setSelected(ServiceInfo s) {
        Log.d("SELECTED", "setSelected: " + s.name);
        selected = s;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selected != null && serviceSelector != null) {
            int pos = inServiceList(selected.name, services);
            serviceSelector.setSelection(pos);
            int count = countHeadersBefore(pos);
            serviceCards.setCurrentItem(pos-count);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        selected = (ServiceInfo) serviceSelector.getSelectedItem();
        ProgressBar progressBar = new ProgressBar(getContext());

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Log.d("SELECTED", "onPageSelected: ");
        scrolled = true;
        int pos = position+countHeadersBefore(position+1);
        serviceSelector.setSelection(pos);
        scrolled = false;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private int countHeadersBefore(int position) {
        int count = 0;
        for (int i = 0; i <= position; i++) {
            if (services.get(i).isHeader()) count++;
        }
        return count;
    }
}

