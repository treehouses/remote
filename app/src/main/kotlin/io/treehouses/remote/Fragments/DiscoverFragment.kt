package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.tabs.TabLayout
import io.treehouses.remote.Views.DiscoverViewPager
import io.treehouses.remote.adapter.DiscoverPageAdapter
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.ServicesListener
import io.treehouses.remote.databinding.ActivityDiscoverFragmentBinding
import io.treehouses.remote.pojo.ServiceInfo

class DiscoverFragment : BaseFragment(), ServicesListener, OnItemSelectedListener, OnPageChangeListener {
    private var discoverView : DiscoverViewPager? = null
    private var tabLayout : TabLayout? = null

    private lateinit var bind: ActivityDiscoverFragmentBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityDiscoverFragmentBinding.inflate(inflater, container, false)

        tabLayout = bind.tabLayout

        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL
        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                replaceFragment(tab!!.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        discoverView = bind.tabViewpager
        discoverView!!.adapter = DiscoverPageAdapter(childFragmentManager)
        discoverView!!.addOnPageChangeListener(this)

        return bind.root
    }

    fun replaceFragment(position: Int) {
        Log.d("DiscoverFragment", position.toString())
        discoverView!!.currentItem = position
    }

    override fun onClick(s: ServiceInfo?) {
        Log.d("DiscoverFragment", "OnClick")
    }

    override fun onPageSelected(position: Int) {
        Log.d("3", "Page selected: ")
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Log.d("DiscoverFragment", "OnNothingSelected")
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        Log.d("DiscoverFragment", "OnItemSelected")
    }

    override fun onPageScrollStateChanged(state: Int) {
        Log.d("DiscoverFragment", "OnPageScrikkStateChanged")
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        tabLayout!!.setScrollPosition(position, 0f, true)
    }


//    private fun switchProfile(profile: NetworkProfile?) {
//        if (profile == null) return
//        progressDialog = ProgressDialog.show(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle), "Connecting...", "Switching to " + profile.ssid, true)
//        progressDialog?.show()
//        when {
//            profile.isWifi -> {
//                //WIFI
//                listener.sendMessage(
//                        getString(if (profile.isHidden) R.string.TREEHOUSES_WIFI_HIDDEN else R.string.TREEHOUSES_WIFI,
//                                profile.ssid, profile.password))
//                networkSsid = profile.ssid
//            }
//            profile.isHotspot -> {
//                //Hotspot
//                listener.sendMessage(
//                        getString(if (profile.isHidden) R.string.TREEHOUSES_AP_HIDDEN else R.string.TREEHOUSES_AP,
//                                profile.option, profile.ssid, profile.password))
//                networkSsid = profile.ssid
//            }
//            profile.isBridge -> {
//                //Bridge
//                listener.sendMessage(getString(R.string.TREEHOUSES_BRIDGE, profile.ssid, profile.hotspot_ssid,
//                        profile.password, profile.hotspot_password))
//            }
//        }
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        if (MainApplication.showLogDialog) {
//            rate(preferences!!)
//            showLogDialog(preferences!!)
//        }
//        activity?.invalidateOptionsMenu()
//    }
//
//    private fun connectRpiListener() {
//        bind.btnConnect.setOnClickListener {
//            val vibe = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//            if (vibe.hasVibrator()) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibe.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
//                else vibe.vibrate(10)
//            }
//            if (connectionState) {
//                RPIDialogFragment.instance!!.bluetoothCheck("unregister")
//                mChatService.stop()
//                connectionState = false
//                checkConnectionState()
//                return@setOnClickListener
//            }
//            if (mBluetoothAdapter?.state == BluetoothAdapter.STATE_OFF) {
//                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
//                Toast.makeText(context, "Bluetooth is disabled", Toast.LENGTH_LONG).show()
//            } else if (mBluetoothAdapter?.state == BluetoothAdapter.STATE_ON) showRPIDialog(this@HomeFragment)
//        }
//    }
//
//    private fun testConnectionListener() {
//        bind.testConnection.setOnClickListener {
//            val preference = PreferenceManager.getDefaultSharedPreferences(context).getString("led_pattern", "LED Heavy Metal")
//            val options = listOf(*resources.getStringArray(R.array.led_options))
//            val optionsCode = resources.getStringArray(R.array.led_options_commands)
//            selectedLed = options.indexOf(preference)
//            listener.sendMessage(optionsCode[selectedLed])
//            testConnectionDialog = showTestConnectionDialog(false, "Testing Connection...", R.string.test_connection_message, selectedLed)
//            testConnectionDialog?.show()
//            testConnectionResult = false
//        }
//    }
//
//    override fun checkConnectionState() {
//        mChatService = listener.getChatService()
//        if (mChatService.state == Constants.STATE_CONNECTED) {
//            showLogDialog(preferences!!)
//            transition(true, arrayOf(150f, 110f, 70f))
//            connectionState = true
//            checkVersionSent = true
//            listener.sendMessage(getString(R.string.TREEHOUSES_REMOTE_VERSION, BuildConfig.VERSION_CODE))
//            Tutorials.homeTutorials(bind, requireActivity())
//        } else {
//            transition(false, arrayOf(0f, 0f, 0f))
//            connectionState = false
//            MainApplication.logSent = false
//        }
//        mChatService.updateHandler(mHandler)
//    }
//
//    private fun transition(connected: Boolean, values: Array<Float>) {
//        bind.btnConnect.text = if (connected) "Disconnect" else "Connect to RPI"
//        bind.btnGetStarted.text = if (connected) "Go to Terminal" else "Get Started"
//        bind.btnConnect.setBackgroundResource(if (connected) R.drawable.ic_disconnect_rpi else R.drawable.ic_connect_to_rpi)
//        bind.backgroundHome.animate().translationY(values[0])
//        bind.btnConnect.animate().translationY(values[1])
//        bind.btnGetStarted.animate().translationY(values[2])
//        val b1 = if (connected) View.GONE else View.VISIBLE     //Show on Boot
//        val b2 = if (connected) View.VISIBLE else View.GONE     //Show when connected
//        bind.welcomeHome.visibility = b1
//        bind.logoHome.visibility = b1
//        bind.testConnection.visibility = b2
//        bind.layoutBack.visibility = b2
//        activity?.invalidateOptionsMenu()
//    }
//
//    private fun dismissTestConnection() {
//        if (testConnectionDialog != null) {
//            testConnectionDialog!!.cancel()
//            showTestConnectionDialog(true, "Process Finished", R.string.test_finished, selectedLed)
//        }
//    }
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        notificationListener = try { getContext() as NotificationCallback?
//        } catch (e: ClassCastException) {
//            throw ClassCastException("Activity must implement NotificationListener")
//        }
//    }
//
//    private fun dismissPDialog() {
//        if (progressDialog != null) progressDialog!!.dismiss()
//    }
//
//    private fun checkVersion(output: String) {
//        checkVersionSent = false
//        if (output.contains("Usage") || output.contains("command")) {
//            showUpgradeCLI()
//        } else if (BuildConfig.VERSION_CODE == 2 || output.contains("true")) {
//            listener.sendMessage(getString(R.string.TREEHOUSES_REMOTE_CHECK))
//        } else if (output.contains("false")) {
//            val alertDialog = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
//                    .setTitle("Update Required")
//                    .setMessage("Please update Treehouses Remote, as it does not meet the required version on the Treehouses CLI.")
//                    .setPositiveButton("Update") { _: DialogInterface?, _: Int ->
//                        val appPackageName = requireActivity().packageName // getPackageName() from Context or Activity object
//                        try {
//                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
//                        } catch (anfe: ActivityNotFoundException) {
//                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
//                        }
//                    }.create()
//            alertDialog.show()
//
//        }
//    }
//
//    private fun readMessage(output: String) {
//        notificationListener = try { context as NotificationCallback?
//        } catch (e: ClassCastException) {
//            throw ClassCastException("Activity must implement NotificationListener")
//        }
//        val s = match(output)
//        when {
//            s == RESULTS.ERROR && !output.toLowerCase(Locale.ROOT).contains("error") -> {
//                showUpgradeCLI()
//                internetSent = false
//            }
//            s == RESULTS.VERSION && checkVersionSent -> checkVersion(output)
//            s == RESULTS.REMOTE_CHECK -> {
//                checkImageInfo(output.trim().split(" "), mChatService.connectedDeviceName)
//                listener.sendMessage(getString(R.string.TREEHOUSES_INTERNET))
//                internetSent = true
//            }
//            s == RESULTS.BOOLEAN && internetSent -> {
//                internetSent = false
//                if (output.contains("true")) internetstatus?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.circle_green))
//                else internetstatus?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.circle_green))
//                listener.sendMessage(getString(R.string.TREEHOUSES_UPGRADE_CHECK))
//            }
//            else -> moreActions(output, s)
//        }
//    }
//
//    private fun moreActions(output: String, result: RESULTS) {
//        when {
//            result == RESULTS.UPGRADE_CHECK -> notificationListener?.setNotification(output.contains("true"))
//            result == RESULTS.HOTSPOT_CONNECTED || result == RESULTS.WIFI_CONNECTED -> {
//                updateStatus("Switched to $networkSsid")
//            }
//            result == RESULTS.BRIDGE_CONNECTED -> {
//                updateStatus("Bridge Has Been Built")
//            }
//            result == RESULTS.DEFAULT_NETWORK -> switchProfile(networkProfile)
//            result == RESULTS.ERROR -> {
//                updateStatus("Network Not Found")
//            }
//            !testConnectionResult -> {
//                testConnectionResult = true
//                dismissTestConnection()
//            }
//        }
//    }
//
//    private fun updateStatus(message : String) {
//        dismissPDialog()
//        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
//    }
//
//    /**
//     * The Handler that gets information back from the BluetoothChatService
//     */
//    private val mHandler: Handler = @SuppressLint("HandlerLeak")
//    object : Handler() {
//        override fun handleMessage(msg: Message) {
//            when (msg.what) {
//                Constants.MESSAGE_READ -> {
//                    val output = msg.obj as String
//                    if (output.isNotEmpty()) {
//                        readMessage(output)
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (mChatService.state == Constants.STATE_CONNECTED) {
//            checkVersionSent = true
//            listener.sendMessage(getString(R.string.TREEHOUSES_REMOTE_VERSION, BuildConfig.VERSION_CODE))
//        }
//    }
//
//    companion object {
//        @JvmField
//        val group_labels = arrayOf("WiFi", "Hotspot", "Bridge", "Default")
//    }
}
