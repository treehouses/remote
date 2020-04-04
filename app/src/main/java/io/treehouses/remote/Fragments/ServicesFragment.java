package io.treehouses.remote.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.bases.BaseServicesFragment;
import io.treehouses.remote.callback.ServicesListener;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesFragment extends BaseServicesFragment implements ServicesListener {

    private static final String TAG = "ServicesFragment";
    
    private FragmentActivity myContext;
    ServicesTabFragment servicesTabFragment;
    ServicesDetailsFragment servicesDetailsFragment;

    View view;
    ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_services_fragment, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = view.findViewById(R.id.pager);
        servicesTabFragment = new ServicesTabFragment();
        servicesDetailsFragment = new ServicesDetailsFragment();

        mChatService = listener.getChatService();
        mChatService.updateHandler(servicesTabFragment.handlerOverview);
        writeToRPI("treehouses remote services available\n");

        // Create an adapter that knows which fragment should be shown on each page
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(getContext(), getChildFragmentManager(), servicesTabFragment, servicesDetailsFragment);

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Give the TabLayout the ViewPager
        tabLayout.setupWithViewPager(viewPager);

        addPageChangeListener();


        return view;
    }

    private void addPageChangeListener() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mChatService = listener.getChatService();
                if (position == 0) {
                    mChatService.updateHandler(servicesTabFragment.handlerOverview);
                    writeToRPI("treehouses remote services available\n");
                }
                else {
                    mChatService.updateHandler(servicesDetailsFragment.handlerDetails);
                    writeToRPI("treehouses version\n");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(ServiceInfo s) {
        Log.d(TAG, "onClick: " + s.name);
        servicesDetailsFragment.setSelected(s);
        viewPager.setCurrentItem(1);
    }

//    public void openCallFragment(Fragment newfragment) {
//        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.fragment_container, newfragment);
//        fragmentTransaction.addToBackStack("");
//        fragmentTransaction.commit();
//    }
}
