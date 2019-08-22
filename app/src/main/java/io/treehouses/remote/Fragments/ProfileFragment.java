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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        listView = view.findViewById(R.id.listViewProfile);

        ArrayList<String> list = new ArrayList<>();

        MyListAdapter adapter = new MyListAdapter(getContext());
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

                if (spinnerProfile.getSelectedItem().toString().equals("Ethernet")) {

                } else if (spinnerProfile.getSelectedItem().equals("Wifi")) {

                } else if (spinnerProfile.getSelectedItem().toString().equals("Hotspot")) {

                } else if (spinnerProfile.getSelectedItem().toString().equals("Bridge")) {

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
