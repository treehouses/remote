package io.treehouses.remote.Fragments;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseServicesFragment;

public class TorTabFragment extends BaseServicesFragment{
    private Button startButton, stopButton;
    View view;
    private ImageView background, logo, internetstatus;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_tor_fragment, container, false);
        logo = view.findViewById(R.id.treehouse_logo);
        startButton = view.findViewById(R.id.btn_tor_start);

        logo.setVisibility(View.VISIBLE);
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        logo.setColorFilter(filter);

        startButton.setOnClickListener(v -> {
            ColorMatrix matrix2 = new ColorMatrix();


            ColorMatrixColorFilter filter2 = new ColorMatrixColorFilter(matrix2);
            logo.setColorFilter(filter2);


        });
        return view;
    }

}
