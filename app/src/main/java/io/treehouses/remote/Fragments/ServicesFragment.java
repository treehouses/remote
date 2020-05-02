package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseServicesFragment;
import io.treehouses.remote.callback.ServicesListener;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesFragment extends BaseServicesFragment implements ServicesListener {

    private static final String TAG = "ServicesFragment";
    private ServicesTabFragment servicesTabFragment;
    private ServicesDetailsFragment servicesDetailsFragment;
    private ProgressBar progressBar;
    private TabLayout tabLayout;

    private ArrayList<ServiceInfo> services;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_services_fragment, container, false);
        services = new ArrayList<>();
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
        setTabEnabled(false);

        progressBar = view.findViewById(R.id.progressBar2);

        mChatService = listener.getChatService();
        mChatService.updateHandler(handler);
        writeToRPI("treehouses remote allservices\n");
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
                    else if(a==0) {
                        progressBar.setVisibility(View.GONE);
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setTitle("Please update CLI")
                                .setMessage("Please update to the latest CLI version to access services.")
                                .create();
                        alertDialog.show();
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    String write_msg = (String) msg.obj;
                    Log.d("WRITE", write_msg);
                    break;
            }
        }
    };

    private void setTabEnabled(boolean enabled) {
        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        tabStrip.setEnabled(enabled);
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setClickable(enabled);
        }
    }

    private void replaceFragment(int position) {
        if (services.isEmpty()) return;
        setTabEnabled(true);

        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = servicesTabFragment;
                mChatService.updateHandler(servicesTabFragment.handlerOverview);
                break;
            case 1:
                fragment = servicesDetailsFragment;
                mChatService.updateHandler(servicesDetailsFragment.handlerDetails);
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
        Objects.requireNonNull(tabLayout.getTabAt(1)).select();
        replaceFragment(1);
    }

}
