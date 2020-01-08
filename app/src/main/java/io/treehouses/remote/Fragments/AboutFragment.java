package io.treehouses.remote.Fragments;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.treehouses.remote.BuildConfig;
import io.treehouses.remote.R;

public class AboutFragment extends androidx.fragment.app.Fragment {

    View view;

    public AboutFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_about, container, false);
        Button gitHub = view.findViewById(R.id.btn_github);
        Button images = view.findViewById(R.id.btn_image);
        Button gitter = view.findViewById(R.id.btn_gitter);
        Button version = view.findViewById(R.id.btn_version);
        Button contributors = view.findViewById(R.id.btn_contributors);
        TextView tvCopyright = view.findViewById(R.id.tv_copyright);
        hyperLinks(gitHub,"https://github.com/treehouses/remote" );
        hyperLinks(images,"https://treehouses.io/#!pages/download.md" );
        hyperLinks(gitter,"https://gitter.im/open-learning-exchange/raspberrypi" );
        hyperLinks(contributors, "https://github.com/treehouses/remote/graphs/contributors");

        version.setOnClickListener(v -> {
            String versionName = BuildConfig.VERSION_NAME;
            if (versionName.equals("1.0.0")) {
                versionName = "latest version";
            }
            Toast.makeText(getContext(), versionName, Toast.LENGTH_LONG).show();
        });

        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        tvCopyright.setText(String.format(getString(R.string.copyright), format.format(new Date()) + ""));
        return view;
    }

    private void hyperLinks(View view , final String url) {
        view.setOnClickListener(view1 -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }
}
