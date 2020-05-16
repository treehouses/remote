package io.treehouses.remote.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import io.treehouses.remote.Fragments.AboutFragment;
import io.treehouses.remote.Fragments.ServiceCardFragment;
import io.treehouses.remote.Fragments.TorTabFragment;
import io.treehouses.remote.Fragments.TunnelOverviewFragment;
import io.treehouses.remote.Fragments.TunnelSSHFragment;
import io.treehouses.remote.pojo.ServiceInfo;

public class TunnelPageAdapter extends FragmentStatePagerAdapter {
    private ArrayList<ServiceInfo> data;

    public TunnelPageAdapter(FragmentManager fm){
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

    }
    public TunnelPageAdapter(FragmentManager fm, ArrayList<ServiceInfo> data) {
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
        return 3;
    }

    public ArrayList<ServiceInfo> getData() {
        return data;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0)
        {
            fragment = new TunnelOverviewFragment();
        }
        else if (position == 1)
        {
            fragment = new TorTabFragment();
        }
        else if (position == 2)
        {
            fragment = new TunnelSSHFragment();
        }
        return fragment;
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
