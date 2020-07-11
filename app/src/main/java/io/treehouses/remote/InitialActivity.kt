package io.treehouses.remote

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import io.treehouses.remote.Fragments.*
import io.treehouses.remote.Fragments.DialogFragments.SSHDialog
import io.treehouses.remote.Fragments.DialogFragments.FeedbackDialogFragment
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.bases.PermissionActivity
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.callback.NotificationCallback
import io.treehouses.remote.databinding.ActivityInitial2Binding
import io.treehouses.remote.utils.GPSService
import io.treehouses.remote.utils.LogUtils

class InitialActivity : PermissionActivity(), NavigationView.OnNavigationItemSelectedListener, HomeInteractListener, NotificationCallback {
    private var validBluetoothConnection = false
    private var mConnectedDeviceName: String? = null
    private lateinit var bind: ActivityInitial2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityInitial2Binding.inflate(layoutInflater)
        instance = this
        setContentView(bind.root)
        requestPermission()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (mChatService == null) {
            mChatService = BluetoothChatService(mHandler, applicationContext)
        } else {
            mChatService!!.updateHandler(mHandler)
        }
        checkStatusNow()
        openCallFragment(HomeFragment())
        setUpDrawer()
        title = "Home"
        GPSService(this)
    }

    private fun setUpDrawer() {
        val toggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(this, bind.drawerLayout, findViewById(R.id.toolbar), R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                (this@InitialActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(window.decorView.windowToken, 0)
            }
        }
        bind.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        bind.navView.setNavigationItemSelectedListener(this)
    }


    override fun onBackPressed() {
        if (bind.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            bind.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val f = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (f is HomeFragment) finish()
            else if (f is SettingsFragment) {
                (supportFragmentManager).popBackStack()
                title = "Home"
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.initial, menu)
        return true
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        checkStatusNow()
        if (validBluetoothConnection) {
            onNavigationItemClicked(id)
        } else {
            when (id) {
                R.id.menu_about -> openCallFragment(AboutFragment())
                R.id.menu_home -> openCallFragment(HomeFragment())
                R.id.menu_ssh -> getSSH()
            }
        }
        title = item.title
        bind.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun onNavigationItemClicked(id: Int) {
        when (id) {
            R.id.menu_home -> openCallFragment(HomeFragment())
            R.id.menu_network -> openCallFragment(NewNetworkFragment())
            R.id.menu_system -> openCallFragment(SystemFragment())
            R.id.menu_terminal -> openCallFragment(TerminalFragment())
            else -> checkMore(id)
        }
    }


    private fun getSSH() {
        SSHDialog(this)
    }

    private fun checkMore(id: Int) {
        when (id) {
            R.id.menu_services -> openCallFragment(ServicesFragment())
            R.id.menu_about -> openCallFragment(AboutFragment())
            R.id.menu_status -> openCallFragment(StatusFragment())
            R.id.menu_tunnel2 -> openCallFragment(SSHTunnelFragment())
            R.id.menu_ssh -> getSSH()
            else -> openCallFragment(HomeFragment())

        }

    }

    override fun openCallFragment(f: Fragment) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, f)
        fragmentTransaction.addToBackStack("")
        fragmentTransaction.commit()
        //        menuItem.setChecked(true);
//        title = "Treehouses Remote"
        //        drawer.closeDrawers();
    }

    //
    override fun setNotification(notificationStatus: Boolean) {
        if (notificationStatus) bind.navView.menu.getItem(6).setIcon(R.drawable.status_notification) else bind.navView.menu.getItem(6).setIcon(R.drawable.status)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 99) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@InitialActivity, "Permissions Granted", Toast.LENGTH_SHORT).show()
            } //TODO re-request
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
        mChatService!!.updateHandler(mHandler)
    }

    override fun setChatService(service: BluetoothChatService) {
        mChatService = service
        mChatService!!.updateHandler(mHandler)
        checkStatusNow()
    }

    override fun getChatService(): BluetoothChatService {
        return mChatService!!
    }

    fun checkStatusNow() {
        validBluetoothConnection = when (mChatService!!.state) {
            Constants.STATE_CONNECTED -> {
                LogUtils.mConnect()
                true
            }
            Constants.STATE_NONE -> {
                LogUtils.mOffline()
                false
            }
            else -> {
                LogUtils.mIdle()
                false
            }
        }
        Log.e("BOOLEAN", "" + validBluetoothConnection)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            openCallFragment(SettingsFragment())
            title = "Settings"
        } else if (item.itemId == R.id.action_feedback) {
            FeedbackDialogFragment().show(supportFragmentManager.beginTransaction(), "feedbackDialogFragment")
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Sends a message.
     *
     * @param s A string of text to send.
     */
    override fun sendMessage(s: String) {
        // Check that we're actually connected before trying anything
        LogUtils.log(s)
        if (mChatService!!.state != Constants.STATE_CONNECTED) {
            Toast.makeText(this@InitialActivity, R.string.not_connected, Toast.LENGTH_SHORT).show()
            LogUtils.mIdle()
            return
        }

        // Check that there's actually something to send
        if (s.isNotEmpty()) {
            // Get the message bytes and tell the BluetoothChatService to write
            val send = s.toByteArray()
            mChatService!!.write(send)

            // Reset out string buffer to zero and clear the edit text field
//            mOutStringBuffer.setLength(0);
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
//            FragmentActivity activity = getActivity();
            //InitialActivity activity = InitialActivity.this;
            when (msg.what) {
                Constants.MESSAGE_DEVICE_NAME -> {
                    // save the connected device's name
                    mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)
                    if (mConnectedDeviceName != "" || mConnectedDeviceName != null) {
                        Log.e("DEVICE", "" + mConnectedDeviceName)
                        checkStatusNow()
                    }
                }
            }
        }
    }

    fun hasValidConnection() : Boolean {
        return validBluetoothConnection
    }

    companion object {
        @JvmStatic
        var instance: InitialActivity? = null
            private set
        private var mChatService: BluetoothChatService? = null
    }
}