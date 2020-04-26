package io.treehouses.remote.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseServicesFragment;
import io.treehouses.remote.callback.ServicesListener;
import io.treehouses.remote.pojo.ServiceInfo;

public class SSHTunnelFragment extends BaseServicesFragment implements ServicesListener {

    View view;
    private TabLayout tabLayout;

    public SSHTunnelFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_ssh_tunnel_fragment, container, false);
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                replaceFragment(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        return view;
    }


    public void replaceFragment(int position) {
        setTabEnabled(true);

        Fragment fragment = null;
        switch (position) {
            case 0:
                Log.d("1", "Overview");

                break;
            case 1:
                Log.d("2", "Tor");
                fragment = new TorTabFragment();
                break;
            case 2:
                Log.d("3", "SSH");
            default:
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_content, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
    public void setTabEnabled(boolean enabled) {
        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        tabStrip.setEnabled(enabled);
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setClickable(enabled);
        }
    }
    @Override
    public void onClick(ServiceInfo s) {
        Log.d("1", "onClick: " + s.name);
        //servicesDetailsFragment.setSelected(s);
        tabLayout.getTabAt(1).select();
        replaceFragment(1);
    }
}