package io.treehouses.remote.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import io.treehouses.remote.Fragments.TorTabFragment;
import io.treehouses.remote.Fragments.TunnelSSHFragment;
import io.treehouses.remote.pojo.ServiceInfo;

public class TunnelPageAdapter extends FragmentStatePagerAdapter {
    private ArrayList<ServiceInfo> data;

    public TunnelPageAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_SET_USER_VISIBLE_HINT);

    }

    public TunnelPageAdapter(FragmentManager fm, ArrayList<ServiceInfo> data) {
        super(fm, BEHAVIOR_SET_USER_VISIBLE_HINT);
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
        return 2;
    }

    public ArrayList<ServiceInfo> getData() {
        return data;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0) {
            fragment = new TorTabFragment();

        } else if (position == 1) {
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
