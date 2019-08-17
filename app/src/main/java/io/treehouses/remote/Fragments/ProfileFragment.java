package io.treehouses.remote.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.treehouses.remote.ButtonConfiguration;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.NetworkListAdapter;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.pojo.NetworkListItem;

public class ProfileFragment extends BaseFragment {

    private static ExpandableListView expandableListView;
    private int lastPosition = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        expandableListView = view.findViewById(R.id.expandableListView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NetworkListAdapter adapter = new NetworkListAdapter(getContext(), NetworkListItem.getProfileList(), mChatService);
        adapter.setListener(listener);
        expandableListView.setAdapter(adapter);
        expandableListView.setGroupIndicator(null);

        expandableListView.setOnGroupExpandListener(groupPosition -> {
            if (lastPosition != -1 && groupPosition != lastPosition) {
                expandableListView.collapseGroup(lastPosition);
            }
            lastPosition = groupPosition;
        });

        spinnerOnSelected(view);
    }

    private void spinnerOnSelected(View view) {
        Spinner spinnerProfile = view.findViewById(R.id.spinnerProfile);
        spinnerProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("TAG", "onItemSelected called");

                if (spinnerProfile.getSelectedItem().toString().equals("Ethernet")) {
                    expandableListView.setVisibility(View.VISIBLE);
                    expandableListView.expandGroup(0);
                } else if (spinnerProfile.getSelectedItem().equals("Wifi")) {
                    expandableListView.setVisibility(View.VISIBLE);
                    expandableListView.expandGroup(1);
                } else if (spinnerProfile.getSelectedItem().toString().equals("Hotspot")) {
                    expandableListView.setVisibility(View.VISIBLE);
                    expandableListView.expandGroup(2);
                } else if (spinnerProfile.getSelectedItem().toString().equals("Bridge")) {
                    expandableListView.setVisibility(View.VISIBLE);
                    expandableListView.expandGroup(3);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
