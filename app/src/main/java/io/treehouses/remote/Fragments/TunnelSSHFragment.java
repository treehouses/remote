package io.treehouses.remote.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;

public class TunnelSSHFragment extends BaseFragment {
    View view;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_tunnel_ssh_fragment, container, false);
        return view;
    }
}
