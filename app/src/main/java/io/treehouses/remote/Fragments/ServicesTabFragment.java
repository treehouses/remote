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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Array;
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
    private int[] versionIntNumber;


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

        versionIntNumber = new int[3];

        ListView listView = view.findViewById(R.id.listView);
        adapter = new ServicesListAdapter(getActivity(), services, getResources().getColor(R.color.bg_white));
        listView.setAdapter(adapter);

        listView.setItemsCanFocus(false);

        listView.setOnItemClickListener(this);

        return view;
    }


    public final Handler handlerOverview = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    String output = (String) msg.obj;
                    performAction(output, tvMessage, progressBar, services, versionIntNumber, adapter);
                    break;
                case Constants.MESSAGE_WRITE:
                    String write_msg = new String((byte[]) msg.obj);
                    Log.d("WRITE", write_msg);
                    break;

            }
        }
    };




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