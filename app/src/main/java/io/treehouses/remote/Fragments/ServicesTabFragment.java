package io.treehouses.remote.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import io.treehouses.remote.R;

public class ServicesTabFragment extends Fragment {

    View view;

    public ServicesTabFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_services_tab_fragment, container, false);

        ArrayList<String> list = new ArrayList<String>();
        list.add("Planet");
        list.add("Planet Test");
        list.add("CouchDB");
        list.add("Moodle");
        list.add("Khan Academy Lite");
        list.add("Kolibri");

        ListView listView = view.findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getListFragment(position);
            }
        });

        return view;
    }

    public void getListFragment(int position){
        switch (position){
            case 0:
                Log.e("Services","Planet");
                break;
            case 1:
                Log.e("Services","Planet Test");
                break;
            case 2:
                Log.e("Services","CouchDB");
                break;
            case 3:
                Log.e("Services","Moodle");
                break;
            case 4:
                Log.e("Services","Khan Academy Lite");
                break;
            case 5:
                Log.e("Services","Kolibri");
                break;
            default:
                Log.e("Default Network Switch", "Nothing...");
        }
    }
}
