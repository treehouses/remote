package io.treehouses.remote.Fragments;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.treehouses.remote.R;

public class AboutFragment extends androidx.fragment.app.Fragment {

    View view;

    public AboutFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.activity_about_fragment, container, false);
        Button gitHub = view.findViewById(R.id.btn_github);
        Button images = view.findViewById(R.id.btn_image);
        Button gitter = view.findViewById(R.id.btn_gitter);
        TextView tvCopyright = view.findViewById(R.id.tv_copyright);
        hyperLinks(gitHub,"https://github.com/treehouses/remote" );
        hyperLinks(images,"http://http://download.treehouses.io" );
        hyperLinks(gitter,"https://gitter.im/open-learning-exchange/raspberrypi" );
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        tvCopyright.setText(String.format(getString(R.string.copyright), format.format(new Date()) + ""));
        return view;
    }

    private void hyperLinks(View view , final String url) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }
}
