package io.treehouses.remote.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.treehouses.remote.MyListAdapter;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;

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

                switchStatment(spinnerProfile, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void switchStatment(Spinner spinnerProfile, int position) {
        switch (spinnerProfile.getItemAtPosition(position).toString()) {
            case "Ethernet":
                setLayout("Ethernet", R.layout.profile_ethernet);
                break;
            case "Wifi":
                setLayout("Wifi", R.layout.profile_wifi);
                break;
            case "Hotspot":
                setLayout("Hotspot", R.layout.profile_hotspot);
                break;
            case "Bridge":
                setLayout("Bridge", R.layout.profile_bridge);
                break;
            case "Tether":
                setLayout("Tether", R.layout.profile_tether);
                break;
        }
    }

    private void setLayout(String value, int layout) {
        MyListAdapter.setLayout(layout);
        MyListAdapter.getMyItems().clear();
        MyListAdapter.setMyItems(value);
        adapter.notifyDataSetChanged();
    }
}
