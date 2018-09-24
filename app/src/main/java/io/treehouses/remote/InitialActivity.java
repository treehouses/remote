package io.treehouses.remote;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import java.util.ArrayList;

import io.treehouses.remote.Fragments.AboutFragment;
import io.treehouses.remote.Fragments.BluetoothChatFragment;
import io.treehouses.remote.Fragments.HomeFragment;
import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.Fragments.ServicesFragment;
import io.treehouses.remote.Fragments.SystemFragment;
import io.treehouses.remote.Fragments.TerminalFragment;

public class InitialActivity extends AppCompatActivity {

    private Toolbar mTopToolbar;
    AccountHeader headerResult;
    private Drawer result = null;
    int REQUEST_COARSE_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        checkLocationPermission();

        mTopToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);
        mTopToolbar.setTitleTextColor(Color.WHITE);
        mTopToolbar.setSubtitleTextColor(Color.WHITE);
        headerResult = getAccountHeader();
        createDrawer();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(R.string.app_project_name);
        openCallFragment(new AboutFragment());
    }

    private AccountHeader getAccountHeader() {
        //Create User profile header
        return new AccountHeaderBuilder()
                .withActivity(InitialActivity.this)
                .withTextColor(getResources().getColor(R.color.md_black_1000))
                .withCloseDrawerOnProfileListClick(false)
                .withSelectionListEnabled(false)
                .addProfiles(
                        new ProfileDrawerItem().withName("You are conected to:").withEmail("RasberryPiModel3B+").withIcon(R.drawable.wifiicon)
                )
                .withCompactStyle(true)
                .withDividerBelowHeader(true)
                .build();

    }
//
    private void createDrawer() {
        com.mikepenz.materialdrawer.holder.DimenHolder dimenHolder = com.mikepenz.materialdrawer.holder.DimenHolder.fromDp(110);
        result = new DrawerBuilder()
                .withActivity(this)
                .withFullscreen(true)
                .withSliderBackgroundColor(getResources().getColor(R.color.colorPrimary))
                .withToolbar(mTopToolbar)
                .withAccountHeader(headerResult)
                .withHeaderHeight(dimenHolder)
                .addDrawerItems(getDrawerItems())
                .withDrawerWidthDp(R.dimen.drawer_width)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            if (drawerItem instanceof Nameable) {
                                menuAction(((Nameable) drawerItem).getName().getTextRes());
                            }
                        }
                        return false;
                    }
                })
                .withDrawerWidthDp(300)
                .build();
    }
    private void menuAction(int selectedMenuId) {
        switch (selectedMenuId) {
            case R.string.menu_network:
                openCallFragment(new NetworkFragment());
                break;
            case R.string.menu_home:
                openCallFragment(new HomeFragment());
                break;
            case R.string.menu_services:
                openCallFragment(new ServicesFragment());
                break;
            case R.string.menu_system:
                openCallFragment(new SystemFragment());
                break;
            case R.string.menu_terminal:
                openCallFragment(new TerminalFragment());
                break;
//            case R.string.menu_courses:
//                openCallFragment(new MyCourseFragment());
//                break;
//            case R.string.menu_feedback:
//                feedbackDialog();
//            case R.string.menu_logout:
//                logout();
//                break;
            default:
                openCallFragment(new AboutFragment());
                break;
        }
    }
    @NonNull
    private IDrawerItem[] getDrawerItems() {
        ArrayList<Drawable> menuImageList = new ArrayList<>();
        menuImageList.add(getResources().getDrawable(R.drawable.home));
        menuImageList.add(getResources().getDrawable(R.drawable.network));
        menuImageList.add(getResources().getDrawable(R.drawable.system));
        menuImageList.add(getResources().getDrawable(R.drawable.terminal));
        menuImageList.add(getResources().getDrawable(R.drawable.circle));
        menuImageList.add(getResources().getDrawable(R.drawable.ssh));
        menuImageList.add(getResources().getDrawable(R.drawable.about));

        return new IDrawerItem[]{
                changeUX(R.string.menu_home, menuImageList.get(0)),
                changeUX(R.string.menu_network, menuImageList.get(1)),
                changeUX(R.string.menu_system, menuImageList.get(2)),
                changeUX(R.string.menu_terminal, menuImageList.get(3)),
                changeUX(R.string.menu_services, menuImageList.get(4)),
                changeUX(R.string.menu_ssh, menuImageList.get(5)),
                changeUX(R.string.menu_about, menuImageList.get(6)),
        };
    }

    public PrimaryDrawerItem changeUX(int iconText, Drawable drawable) {
        return new PrimaryDrawerItem().withName(iconText)
                .withIcon(drawable).withTextColor(getResources().getColor(R.color.textColorPrimary))
                .withIconColor(getResources().getColor(R.color.textColorPrimary))
                .withSelectedIconColor(getResources().getColor(R.color.primary_dark))
                .withIconTintingEnabled(true);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    public void openCallFragment(Fragment newfragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, newfragment);
        fragmentTransaction.addToBackStack("");
        fragmentTransaction.commit();
    }
    protected void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 99: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    //TODO re-request
                }
                break;
            }
        }
    }
}
