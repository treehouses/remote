package io.treehouses.remote.Fragments;

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
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.ServicesListAdapter;
import io.treehouses.remote.bases.BaseFragment;

public class ServicesTabFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private View view;
    private ProgressBar progressBar;
    private ArrayList<String> services;
    ArrayAdapter<String> adapter;

    public ServicesTabFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);

        writeToRPI("treehouses remote services available");

        view = inflater.inflate(R.layout.activity_services_tab_fragment, container, false);
        progressBar = view.findViewById(R.id.progress_services);
        progressBar.setVisibility(View.VISIBLE);
        services = new ArrayList<String>();

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


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    String output = (String) msg.obj;
                    if (!output.isEmpty() && output.startsWith("Available")) {
                        //Read
                        progressBar.setVisibility(View.GONE);
                        services.addAll(Arrays.asList(output.substring(11).split(" ")));
                        adapter.notifyDataSetChanged();
                    }
            }
        }
    };

    private void performService(String action, String command, String name) {
        Log.d("SERVICES", action +" "+ name);
        Toast.makeText(getContext(), name + " " + action, Toast.LENGTH_LONG).show();
        writeToRPI(command);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (view.getId()) {
            case R.id.start_service:
                performService("Started", "treehouses services "+services.get(position) + " start", services.get(position));
                break;
            case R.id.stop_service:
                performService("Stopped", "treehouses services "+services.get(position) + " stop", services.get(position));
                break;
            case R.id.install_service:
                performService("Installed", "treehouses services "+services.get(position) + " up", services.get(position));
                break;
            case R.id.uninstall_service:
                performService("Uninstalled", "treehouses services "+services.get(position) + " down", services.get(position));
                break;
        }
    }



}

