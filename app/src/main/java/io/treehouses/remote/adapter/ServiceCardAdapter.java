package io.treehouses.remote.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import io.treehouses.remote.Fragments.ServiceCardFragment;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServiceCardAdapter extends FragmentStatePagerAdapter {
    private ArrayList<ServiceInfo> data;

    public ServiceCardAdapter(FragmentManager fm, ArrayList<ServiceInfo> data) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.data = removeHeaders(data);

    }

    private ArrayList<ServiceInfo> removeHeaders(ArrayList<ServiceInfo> data) {
        ArrayList<ServiceInfo> tmp = new ArrayList<>(data);
        Iterator<ServiceInfo> iterator = tmp.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isHeader()) iterator.remove();
        }
        return tmp;
    }

    @Override
    public int getCount() {
        return data.size();
    }
    public ArrayList<ServiceInfo> getData() {
        return data;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        Bundle b = new Bundle();
        b.putSerializable("serviceData",data.get(position));
        Fragment f = new ServiceCardFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public int getItemPosition(Object o) {
        return POSITION_NONE;
    }


    @Override
    public void notifyDataSetChanged() {
        Collections.sort(data);
        super.notifyDataSetChanged();
    }


}
