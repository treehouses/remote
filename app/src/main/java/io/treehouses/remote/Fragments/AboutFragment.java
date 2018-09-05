package io.treehouses.remote.Fragments;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import io.treehouses.remote.Network.DeviceListActivity;
import io.treehouses.remote.R;

public class AboutFragment extends Fragment {

    public AboutFragment(){}

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.activity_about_fragment, container, false);

//        Button rpi = view.findViewById(R.id.version);
//
//        rpi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), DeviceListActivity.class);
//                startActivity(intent);
//            }
//        });
        return view;
    }
}
