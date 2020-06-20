package io.treehouses.remote.Fragments;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.ServicesListAdapter;
import io.treehouses.remote.bases.BaseServicesFragment;
import io.treehouses.remote.callback.ServicesListener;
import io.treehouses.remote.databinding.ActivityServicesTabFragmentBinding;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesTabFragment extends BaseServicesFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "ServicesTabFragment";
    private View view;
    public ArrayList<ServiceInfo> services;
    private ServicesListAdapter adapter;
    private ServicesListener servicesListener;
    private int used = 0, total = 1;

    private ActivityServicesTabFragmentBinding bind;


    public ServicesTabFragment(ArrayList<ServiceInfo> serviceInfos) {
        this.services = serviceInfos;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mChatService = listener.getChatService();

        bind = ActivityServicesTabFragmentBinding.inflate(inflater, container, false);

        adapter = new ServicesListAdapter(getActivity(), services, getResources().getColor(R.color.bg_white));
        bind.listView.setAdapter(adapter);

        bind.listView.setOnItemClickListener(this);

        return bind.getRoot();
    }
        public final Handler handlerOverview = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    String output = (String) msg.obj;
                    moreAction(output);
                    break;
                case Constants.MESSAGE_WRITE:
                    String write_msg = new String((byte[]) msg.obj);
                    Log.d("WRITE", write_msg);
                    break;

            }
        }
    };

    private void moreAction(String output) {
        int memoryPercentage = 0;
        try {
            int i = Integer.parseInt(output.trim());
            if (i >= total) {
                total = i;
                writeToRPI("treehouses memory used");
            } else {
                used = i;
                memoryPercentage = (int)(((float) used / total) * 100);
                String strMemoryPercentage = memoryPercentage + "%";
                bind.memoryPercentage.setText(strMemoryPercentage);
                ObjectAnimator.ofInt(bind.spaceLeft, "progress", memoryPercentage)
                        .setDuration(600)
                        .start();
            }
        } catch (NumberFormatException ignored) {
        }
        Log.d(TAG, "moreAction: " + String.format("Used: %d / %d ", used, total) + memoryPercentage + "%");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            servicesListener = (ServicesListener) getParentFragment();
        }
        catch (ClassCastException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ServiceInfo selected = services.get(position);
        if (servicesListener != null) servicesListener.onClick(selected);
    }

    @Override
    public void onResume() {
        super.onResume();
        writeToRPI("treehouses memory total\n");
    }

}