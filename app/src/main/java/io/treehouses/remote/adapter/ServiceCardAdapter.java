package io.treehouses.remote.adapter;

import android.app.Service;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
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

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return new ServiceCardFragment(data.get(position));
    }

}
