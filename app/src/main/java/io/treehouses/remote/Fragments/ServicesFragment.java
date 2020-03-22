package io.treehouses.remote.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.bases.BaseServicesFragment;

public class ServicesFragment extends BaseServicesFragment {

    private FragmentActivity myContext;
    ServicesTabFragment servicesTabFragment;
    ServicesDetailsFragment servicesDetailsFragment;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_services_fragment, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        ViewPager viewPager = view.findViewById(R.id.pager);
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

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mChatService = listener.getChatService();
                    mChatService.updateHandler(servicesTabFragment.handlerOverview);
                    writeToRPI("treehouses remote services available\n");
                }
                else {
                    mChatService = listener.getChatService();
                    mChatService.updateHandler(servicesDetailsFragment.handlerDetails);
                    writeToRPI("treehouses version\n");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        return view;
    }

//    public void openCallFragment(Fragment newfragment) {
//        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.fragment_container, newfragment);
//        fragmentTransaction.addToBackStack("");
//        fragmentTransaction.commit();
//    }
}
