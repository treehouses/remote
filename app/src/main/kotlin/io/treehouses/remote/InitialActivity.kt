package io.treehouses.remote

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import io.treehouses.remote.MainApplication
import io.treehouses.remote.callback.BackPressReceiver
import io.treehouses.remote.databinding.ActivityInitial2Binding
import io.treehouses.remote.fragments.CommunityFragment
import io.treehouses.remote.fragments.DiscoverFragment
import io.treehouses.remote.fragments.SettingsFragment
import io.treehouses.remote.fragments.ShowBluetoothFileFragment
import io.treehouses.remote.fragments.dialogfragments.FeedbackDialogFragment
import io.treehouses.remote.ui.home.HomeFragment
import io.treehouses.remote.utils.DialogUtils
import io.treehouses.remote.utils.GPSService

class InitialActivity : BaseInitialActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityInitial2Binding.inflate(layoutInflater)
        instance = this
        setContentView(bind.root)
        requestPermission()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        currentTitle = "Home"
        setUpDrawer()
        title = "Home"

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startGPSService()
        }

        mChatService.updateHandler(mHandler)
        checkStatusNow()
        openCallFragment(HomeFragment())

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleCustomOnBackPressed()
            }
        })
    }

    private fun startGPSService() {
        val intent = Intent(this, GPSService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        resetMenuIcon()
    }

    private fun setUpDrawer() {
        mActionBarDrawerToggle = object : ActionBarDrawerToggle(this, bind.drawerLayout, findViewById(R.id.toolbar), R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                (this@InitialActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(window.decorView.windowToken, 0)
            }
        }
        bind.drawerLayout.addDrawerListener(mActionBarDrawerToggle)
        mActionBarDrawerToggle.syncState()
        bind.navView.setNavigationItemSelectedListener(this)
    }

    fun handleCustomOnBackPressed() {
        if (bind.drawerLayout.isDrawerOpen(GravityCompat.START)) bind.drawerLayout.closeDrawer(GravityCompat.START)
        else {
            val f = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (f is HomeFragment) finishAffinity()
            else if (f is SettingsFragment || f is CommunityFragment || f is DiscoverFragment || f is ShowBluetoothFileFragment) {
                (supportFragmentManager).popBackStack()
                title = currentTitle
            }
            if (f is BackPressReceiver) f.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.initial, menu)
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION_FOR_COMMUNITY -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    preferences?.edit()?.putBoolean("send_log", true)?.apply()
                    goToCommunity()
                } else {
                    Toast.makeText(this, "Permission denied. Cannot proceed to community features.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        checkStatusNow()
        for (x in 1 until bind.navView.menu.size() - 2) {
            val item = bind.navView.menu.getItem(x)
            item.isEnabled = validBluetoothConnection
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mChatService.updateHandler(mHandler)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                openCallFragment(SettingsFragment())
                title = getString(R.string.action_settings)
            }
            R.id.action_feedback -> {
                FeedbackDialogFragment().show(supportFragmentManager.beginTransaction(), "feedbackDialogFragment")
            }
            R.id.action_community -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    dataSharing()
                } else {
                    preferences?.edit()?.putBoolean("send_log", true)?.apply()
                    goToCommunity()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun dataSharing() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val v = layoutInflater.inflate(R.layout.alert_log_map, null)
        if (preferences?.getBoolean("send_log", false) == false) {
            val builder = DialogUtils.createAlertDialog(this@InitialActivity,
                "Sharing is Caring",
                "To enable the community map, Treehouses needs to collect some data and your approximate location. Would you like to enable data sharing?", v)
                .setCancelable(false)
            DialogUtils.createAdvancedDialog(builder, Pair("Enable Data Sharing", "Cancel"), {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION_FOR_COMMUNITY)
            }, {MainApplication.showLogDialog = false })
        } else {
            goToCommunity()
        }
    }

    private fun goToCommunity() {
        openCallFragment(CommunityFragment())
        title = getString(R.string.action_community)
    }

    private fun showLocationPermissionDisclosureForCommunity() {
        AlertDialog.Builder(this)
            .setTitle("Location & GPS Usage")
            .setMessage("This app collects location data to create a community map and understand user distribution. " +
                    "This helps us improve our services. To continue, please enable GPS.")
            .setPositiveButton("Ok") { _, _ ->
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION_FOR_COMMUNITY)
            }
                .setNegativeButton("No", null).show()
    }

    fun changeAppBar() {
        mActionBarDrawerToggle = ActionBarDrawerToggle(this, bind.drawerLayout, findViewById(R.id.toolbar), 0, 0)
        mActionBarDrawerToggle.toolbarNavigationClickListener = View.OnClickListener {
            //reset to burger icon
            supportFragmentManager.popBackStack()
            resetMenuIcon()
        }
        //add back button
        bind.drawerLayout.setDrawerListener(mActionBarDrawerToggle)
        mActionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActionBarDrawerToggle.isDrawerIndicatorEnabled = false
        bind.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun resetMenuIcon() {
        mActionBarDrawerToggle.isDrawerIndicatorEnabled = true
        bind.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION_FOR_COMMUNITY = 2
    }
}
