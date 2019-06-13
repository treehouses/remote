package io.treehouses.remote.Fragments;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import io.treehouses.remote.R;

public class AboutFragment extends androidx.fragment.app.Fragment {

    private Button gitHub, images, gitter;
    View view;

    public AboutFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.activity_about_fragment, container, false);

        hyperLinks();
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

    private void hyperLinks() {
        gitHub = view.findViewById(R.id.btn_github);
        images = view.findViewById(R.id.btn_image);
        gitter = view.findViewById(R.id.btn_gitter);

       // gitHub.setClickable(true);
        gitHub.setMovementMethod(LinkMovementMethod.getInstance());
        gitHub.setText(Html.fromHtml("<a href='https://github.com/treehouses/remote'>GitHub</a>"));

        images.setMovementMethod(LinkMovementMethod.getInstance());
        images.setText(Html.fromHtml("<a href='http://http://download.treehouses.io'>Treehouses Images</a>"));

        gitter.setMovementMethod(LinkMovementMethod.getInstance());
        gitter.setText(Html.fromHtml("<a href='https://gitter.im/open-learning-exchange/raspberrypi'>Gitter</a>"));
    }
}
