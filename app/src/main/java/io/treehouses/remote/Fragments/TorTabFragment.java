package io.treehouses.remote.Fragments;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseServicesFragment;

public class TorTabFragment extends BaseServicesFragment{
    private Button startButton, stopButton;
    private TextView textStatus;
    View view;
    private ImageView background, logo, internetstatus;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_tor_fragment, container, false);
        logo = view.findViewById(R.id.treehouse_logo);
        startButton = view.findViewById(R.id.btn_tor_start);
        textStatus = view.findViewById(R.id.tor_status_text);

        logo.setVisibility(View.VISIBLE);
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        textStatus.setText("Status:Off");

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);



            logo.setColorFilter(filter);

            startButton.setOnClickListener(v -> {
                if(textStatus.getText() == "Status:On"){
                    textStatus.setText("Status:Off");
                    logo.setColorFilter(filter);
                    startButton.setText("Start Tor");

                }
                else if(textStatus.getText() == "Status:Off") {
                    ColorMatrix matrix2 = new ColorMatrix();


                    ColorMatrixColorFilter filter2 = new ColorMatrixColorFilter(matrix2);
                    logo.setColorFilter(filter2);
                    textStatus.setText("Status:On");
                    startButton.setText("Stop Tor");
                }

            });

        return view;
    }

}
