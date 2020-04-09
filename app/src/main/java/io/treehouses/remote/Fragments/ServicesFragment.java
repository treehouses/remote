package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.bases.BaseServicesFragment;
import io.treehouses.remote.callback.ServicesListener;
import io.treehouses.remote.pojo.ServiceInfo;
import io.treehouses.remote.pojo.ServicesData;

public class ServicesFragment extends BaseServicesFragment implements ServicesListener {

    private static final String TAG = "ServicesFragment";
    
    private FragmentActivity myContext;
    ServicesTabFragment servicesTabFragment;
    ServicesDetailsFragment servicesDetailsFragment;
    ProgressBar progressBar;

    private ServicesData servicesData;
    private ArrayList<ServiceInfo> services;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_services_fragment, container, false);
        services = new ArrayList<>();
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
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
        progressBar = view.findViewById(R.id.progressBar2);

//        viewPager = view.findViewById(R.id.main_content);

        mChatService = listener.getChatService();
        mChatService.updateHandler(handler);
        writeToRPI("treehouses remote json\n");

        return view;
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    String output = (String) msg.obj;
                    int a = performAction(output, services);
                    if (a == 1) {
                        servicesTabFragment = new ServicesTabFragment(services);
                        servicesDetailsFragment = new ServicesDetailsFragment(services);
                        progressBar.setVisibility(View.GONE);
                        replaceFragment(0);
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    String write_msg = new String((byte[]) msg.obj);
                    Log.d("WRITE", write_msg);
                    break;
            }
        }
    };

    public void replaceFragment(int position) {
//        if (servicesData == null) return;

        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = servicesTabFragment;
//                mChatService.updateHandler(servicesTabFragment.handlerOverview);
                break;
            case 1:
                fragment = servicesDetailsFragment;
//                mChatService.updateHandler(servicesDetailsFragment.handlerDetails);
                break;
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


    @Override
    public void onClick(ServiceInfo s) {
        Log.d(TAG, "onClick: " + s.name);
        servicesDetailsFragment.setSelected(s);
        replaceFragment(1);
    }

}
