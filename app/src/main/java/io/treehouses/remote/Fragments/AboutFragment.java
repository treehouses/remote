package io.treehouses.remote.Fragments;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
