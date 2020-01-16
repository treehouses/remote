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

        writeToRPI("treehouses services available string");

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
                    if (!output.isEmpty()) {
                        //Read
                        progressBar.setVisibility(View.GONE);
                        services.addAll(Arrays.asList(output.split(" ")));
                        adapter.notifyDataSetChanged();
                    }
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("CLICKED", "SDF");
        switch (view.getId()) {
            case R.id.start_service:
                Log.d("SERVICES", "START " + services.get(position));
                Toast.makeText(getContext(), services.get(position) + " Started", Toast.LENGTH_LONG).show();
                writeToRPI("treehouses services "+services.get(position) + " up");
                break;
            case R.id.stop_service:
                Log.d("SERVICES", "STOP " + services.get(position));
                writeToRPI("treehouses services "+services.get(position) + " stop");
                Toast.makeText(getContext(), services.get(position) + " Stopped", Toast.LENGTH_LONG).show();
                break;
            case R.id.install_service:
                Log.d("SERVICES", "INSTALL " + services.get(position));
                writeToRPI("treehouses services "+services.get(position) + " up");
                Toast.makeText(getContext(), services.get(position) + " Installed", Toast.LENGTH_LONG).show();
                break;
            case R.id.uninstall_service:
                Log.d("SERVICES", "UNINSTALL " + services.get(position));
                writeToRPI("treehouses services "+services.get(position) + " down");
                Toast.makeText(getContext(), services.get(position) + " Uninstalled", Toast.LENGTH_LONG).show();

                break;
        }
    }



}

