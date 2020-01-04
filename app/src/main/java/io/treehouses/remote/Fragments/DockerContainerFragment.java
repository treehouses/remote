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

public class DockerContainerFragment extends Fragment{

    View view;

    public DockerContainerFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_docker_container_fragment, container, false);

        ArrayList<String> list = new ArrayList<String>();
        list.add("housing_django_1");
        list.add("housing_postgres_1");

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
                Log.e("Docker Containers", "housing_django_1");
                break;
            case 1:
                Log.e("Docker Containers", "housing_postgres_1");
                break;
            default:
                Log.e("Default Network Switch", "Nothing...");
        }
    }

}
