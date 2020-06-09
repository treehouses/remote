package io.treehouses.remote.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import io.treehouses.remote.R;
import io.treehouses.remote.Views.TunnelViewPager;
import io.treehouses.remote.adapter.TunnelPageAdapter;
import io.treehouses.remote.bases.BaseServicesFragment;
import io.treehouses.remote.callback.ServicesListener;
import io.treehouses.remote.pojo.ServiceInfo;

public class SSHTunnelFragment extends BaseServicesFragment implements ServicesListener, AdapterView.OnItemSelectedListener, ViewPager.OnPageChangeListener {

    View view;
    private TabLayout tabLayout;
    private TunnelViewPager tunnelView;
    private TunnelPageAdapter tunnelPageAdapter;

    public SSHTunnelFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_ssh_tunnel_fragment, container, false);
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                replaceFragment(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        tunnelView = view.findViewById(R.id.tab_viewpager);

        tunnelPageAdapter = new TunnelPageAdapter(getChildFragmentManager());
        tunnelView.setAdapter(tunnelPageAdapter);

        tunnelView.addOnPageChangeListener(this);
        return view;
    }


    public void replaceFragment(int position) {
        Log.d("dasd", String.valueOf(position));
        tunnelView.setCurrentItem(position);
    }
//

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("3", "onItemSelected: ");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d("3", "onNothing: ");
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Log.d("3", "onItemscrolled: ");
        tabLayout.setScrollPosition(position,0f,true);

    }

    @Override
    public void onPageSelected(int position) {
        Log.d("3", "Page selected: ");
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}