package io.treehouses.remote.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.treehouses.remote.R;

import static android.content.Context.WIFI_SERVICE;

public class TunnelFragment extends androidx.fragment.app.Fragment {

    View view;

    public TunnelFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_tunnel_fragment, container, false);

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
