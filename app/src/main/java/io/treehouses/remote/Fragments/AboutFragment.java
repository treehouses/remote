package io.treehouses.remote.Fragments;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.treehouses.remote.R;

public class AboutFragment extends Fragment {

    public AboutFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_about_fragment, container, false);
    }
}
