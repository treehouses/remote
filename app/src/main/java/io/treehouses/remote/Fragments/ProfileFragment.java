package io.treehouses.remote.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.treehouses.remote.ButtonConfiguration;
import io.treehouses.remote.MyListAdapter;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.NetworkListAdapter;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.pojo.NetworkListItem;

import static android.R.layout.simple_list_item_1;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class ProfileFragment extends BaseFragment {

    private ListView listView;
    private MyListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        listView = view.findViewById(R.id.listViewProfile);

        adapter = new MyListAdapter(getContext());
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerOnSelected(view);
    }

    private void spinnerOnSelected(View view) {
        Spinner spinnerProfile = view.findViewById(R.id.spinnerProfile);
        spinnerProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("TAG", "onItemSelected called");

                if (spinnerProfile.getItemAtPosition(position).equals("Ethernet")) {
                    MyListAdapter.setLayout(R.layout.profile_ethernet);
                    MyListAdapter.getMyItems().clear();
                    MyListAdapter.setMyItems("Ethernet1");
                    MyListAdapter.setMyItems("Ethernet2");
                    MyListAdapter.setMyItems("Ethernet3");
                    MyListAdapter.setMyItems("Ethernet4");

                    adapter.notifyDataSetChanged();
                } else if (spinnerProfile.getItemAtPosition(position).equals("Wifi")) {
                    MyListAdapter.setLayout(R.layout.profile_wifi);
                    MyListAdapter.getMyItems().clear();
                    MyListAdapter.setMyItems("Wifi1");
                    MyListAdapter.setMyItems("Wifi2");
                    adapter.notifyDataSetChanged();
                } else if (spinnerProfile.getItemAtPosition(position).equals("Hotspot")) {
                    MyListAdapter.setLayout(R.layout.profile_hotspot);
                    MyListAdapter.getMyItems().clear();
                    MyListAdapter.setMyItems("Hotspot1");
                    MyListAdapter.setMyItems("Hotspot2");
                    MyListAdapter.setMyItems("Hotspot3");

                    adapter.notifyDataSetChanged();
                } else if (spinnerProfile.getItemAtPosition(position).equals("Bridge")) {
                    MyListAdapter.setLayout(R.layout.profile_bridge);
                    MyListAdapter.getMyItems().clear();
                    MyListAdapter.setMyItems("Bridge1");
                    MyListAdapter.setMyItems("Bridge2");
                    MyListAdapter.setMyItems("Bridge3");
                    MyListAdapter.setMyItems("Bridge4");

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
