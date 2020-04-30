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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs.EthernetBottomSheet;
import io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs.TorBottomSheet;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseServicesFragment;

public class TorTabFragment extends BaseServicesFragment{
    private Button startButton, moreButton;
    private TextView textStatus;
    View view;
    private ImageView background, logo, internetstatus;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_tor_fragment, container, false);
        logo = view.findViewById(R.id.treehouse_logo);
        startButton = view.findViewById(R.id.btn_tor_start);
        moreButton = view.findViewById(R.id.btn_tor_more);
        textStatus = view.findViewById(R.id.tor_status_text);

        logo.setVisibility(View.VISIBLE);
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        textStatus.setText("Status:Off");
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            logo.setColorFilter(filter);
             /* start/stop tor button click */
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
            /* more button click */
            moreButton.setOnClickListener(v ->{
                showBottomSheet(new TorBottomSheet(), "ethernet");
            });


        return view;
    }
    private void showBottomSheet(BottomSheetDialogFragment fragment, String tag) {
        fragment.setTargetFragment(TorTabFragment.this, Constants.NETWORK_BOTTOM_SHEET);
        fragment.show(getFragmentManager(), tag);
    }
}
