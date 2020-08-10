package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.*
import androidx.preference.PreferenceManager
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import io.treehouses.remote.*
import io.treehouses.remote.Constants.REQUEST_ENABLE_BT
import io.treehouses.remote.Fragments.DialogFragments.RPIDialogFragment
import io.treehouses.remote.InitialActivity.Companion.instance
import io.treehouses.remote.adapter.ProfilesListAdapter
import io.treehouses.remote.bases.BaseHomeFragment
import io.treehouses.remote.callback.NotificationCallback
import io.treehouses.remote.callback.SetDisconnect
import io.treehouses.remote.databinding.ActivityHomeFragmentBinding
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.SaveUtils
import io.treehouses.remote.utils.match
import io.treehouses.remote.utils.Utils.toast
import kotlinx.android.synthetic.main.activity_home_fragment.*
import java.util.*

class HomeFragment : BaseHomeFragment(), SetDisconnect {
    private var notificationListener: NotificationCallback? = null
    private var progressDialog: ProgressDialog? = null
    private var connectionState = false
    private var testConnectionResult = false
    private var testConnectionDialog: AlertDialog? = null
    private var selectedLed = 0
    private var checkVersionSent = false
    private var internetSent = false
    private var hashSent = false
    private var networkSsid = ""
    private var networkProfile: NetworkProfile? = null

    private lateinit var bind: ActivityHomeFragmentBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityHomeFragmentBinding.inflate(inflater, container, false)
        mChatService = listener.getChatService()
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        setupProfiles()
        showDialogOnce(preferences!!)
        checkConnectionState()
        connectRpiListener()
        bind.btnGetStarted.setOnClickListener {
            instance!!.checkStatusNow()
            if (instance!!.hasValidConnection()) {
                switchFragment(TerminalFragment(), "Terminal")
            } else {
                switchFragment(AboutFragment(), "About")
            }
        }
        testConnectionListener()
        return bind.root
    }

    private fun switchFragment(fragment: Fragment, title: String) {
        instance!!.openCallFragment(fragment)
        activity?.let { it.title = title}
    }

    private fun setupProfiles() {
        val profileAdapter = ProfilesListAdapter(requireContext(), listOf(*group_labels), SaveUtils.getProfiles(requireContext()))
        bind.networkProfiles.setAdapter(profileAdapter)
        bind.networkProfiles.setOnChildClickListener { _: ExpandableListView?, _: View?, groupPosition: Int, childPosition: Int, _: Long ->
            if (groupPosition == 3) {
                listener.sendMessage(getString(R.string.TREEHOUSES_DEFAULT_NETWORK))
                context.toast("Switched to Default Network", Toast.LENGTH_LONG)
            } else if (SaveUtils.getProfiles(requireContext()).size > 0 && SaveUtils.getProfiles(requireContext())[listOf(*group_labels)[groupPosition]]!!.isNotEmpty()) {
                if (SaveUtils.getProfiles(requireContext())[listOf(*group_labels)[groupPosition]]!!.size <= childPosition) return@setOnChildClickListener false
                networkProfile = SaveUtils.getProfiles(requireContext())[listOf(*group_labels)[groupPosition]]!![childPosition]
                listener.sendMessage(getString(R.string.TREEHOUSES_DEFAULT_NETWORK))
                requireContext().toast("Configuring...", Toast.LENGTH_LONG)
            }
            false
        }
    }

    private fun switchProfile(profile: NetworkProfile?) {
        if (profile == null) return
        progressDialog = ProgressDialog.show(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle), "Connecting...", "Switching to " + profile.ssid, true)
        progressDialog?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        progressDialog?.show()
        when {
            profile.isWifi -> {
                //WIFI
                listener.sendMessage(
                        getString(if (profile.isHidden) R.string.TREEHOUSES_WIFI_HIDDEN else R.string.TREEHOUSES_WIFI,
                        profile.ssid, profile.password))
                networkSsid = profile.ssid
            }
            profile.isHotspot -> {
                //Hotspot
                listener.sendMessage(
                        getString(if (profile.isHidden) R.string.TREEHOUSES_AP_HIDDEN else R.string.TREEHOUSES_AP,
                        profile.option, profile.ssid, profile.password))
                networkSsid = profile.ssid
            }
            profile.isBridge -> {
                //Bridge
                listener.sendMessage(getString(R.string.TREEHOUSES_BRIDGE, profile.ssid, profile.hotspot_ssid,
                        profile.password, profile.hotspot_password))
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (MainApplication.showLogDialog) {
            rate(preferences!!)
            showLogDialog(preferences!!)
        }
        activity?.invalidateOptionsMenu()
    }

    private fun connectRpiListener() {
        bind.btnConnect.setOnClickListener {
            val vibe = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibe.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibe.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                else vibe.vibrate(10)
            }
            if (connectionState) {
                RPIDialogFragment.instance!!.bluetoothCheck("unregister")
                mChatService.stop()
                connectionState = false
                checkConnectionState()
                return@setOnClickListener
            }
            if (mBluetoothAdapter?.state == BluetoothAdapter.STATE_OFF) {
                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
                context.toast( "Bluetooth is disabled", Toast.LENGTH_LONG)
            } else if (mBluetoothAdapter?.state == BluetoothAdapter.STATE_ON) showRPIDialog(this@HomeFragment)
        }
    }

    private fun testConnectionListener() {
        bind.testConnection.setOnClickListener {
            val preference = PreferenceManager.getDefaultSharedPreferences(context).getString("led_pattern", "LED Heavy Metal")
            val options = listOf(*resources.getStringArray(R.array.led_options))
            val optionsCode = resources.getStringArray(R.array.led_options_commands)
            selectedLed = options.indexOf(preference)
            listener.sendMessage(optionsCode[selectedLed])
            testConnectionDialog = showTestConnectionDialog(false, "Testing Connection...", R.string.test_connection_message, selectedLed)
            testConnectionDialog?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            testConnectionDialog?.show()
            testConnectionResult = false
        }
    }

    override fun checkConnectionState() {
        mChatService = listener.getChatService()
        if (mChatService.state == Constants.STATE_CONNECTED) {
            showLogDialog(preferences!!)
            transition(true)
            connectionState = true
            listener.sendMessage("remotehash")
            hashSent = true
            Tutorials.homeTutorials(bind, requireActivity())
        } else {
            transition(false)
            connectionState = false
            hashSent = false
            MainApplication.logSent = false
        }
        mChatService.updateHandler(mHandler)
    }

    private fun transition(connected: Boolean) {
        bind.btnConnect.text = if (connected) "Disconnect" else "Connect to RPI"
        bind.btnGetStarted.text = if (connected) "Go to Terminal" else "Get Started"
        bind.btnConnect.setBackgroundResource(if (connected) R.drawable.ic_disconnect_rpi else R.drawable.ic_connect_to_rpi)
        val b1 = if (connected) View.GONE else View.VISIBLE     //Show on Boot
        val b2 = if (connected) View.VISIBLE else View.GONE     //Show when connected
        bind.welcomeHome.visibility = b1
        bind.logoHome.visibility = b1
        bind.testConnection.visibility = b2
        bind.layoutBack.visibility = b2
        activity?.invalidateOptionsMenu()
    }

    private fun dismissTestConnection() {
        if (testConnectionDialog != null) {
            testConnectionDialog!!.cancel()
            showTestConnectionDialog(true, "Process Finished", R.string.test_finished, selectedLed)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        notificationListener = try { getContext() as NotificationCallback?
        } catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement NotificationListener")
        }
    }

    private fun dismissPDialog() {
        if (progressDialog != null) progressDialog!!.dismiss()
    }

    private fun checkVersion(output: String) {
        checkVersionSent = false
        if (output.contains("Usage") || output.contains("command")) {
            showUpgradeCLI()
        } else if (BuildConfig.VERSION_CODE == 2 || output.contains("true")) {
            listener.sendMessage(getString(R.string.TREEHOUSES_REMOTE_CHECK))
        } else if (output.contains("false")) {
            val alertDialog = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
                    .setTitle("Update Required")
                    .setMessage("Please update Treehouses Remote, as it does not meet the required version on the Treehouses CLI.")
                    .setPositiveButton("Update") { _: DialogInterface?, _: Int ->
                        val appPackageName = requireActivity().packageName // getPackageName() from Context or Activity object
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                        } catch (anfe: ActivityNotFoundException) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                        }
                    }.create()
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

        }
    }

    private fun readMessage(output: String) {
        notificationListener = try { context as NotificationCallback?
        } catch (e: ClassCastException) { throw ClassCastException("Activity must implement NotificationListener") }
        val s = match(output)
        when {
            hashSent -> {
                syncBluetooth(output)
                hashSent = false
                checkVersionSent = true
                listener.sendMessage(getString(R.string.TREEHOUSES_REMOTE_VERSION, BuildConfig.VERSION_CODE))
            }
            s == RESULTS.ERROR && !output.toLowerCase(Locale.ROOT).contains("error") -> {
                showUpgradeCLI()
                internetSent = false
            }
            s == RESULTS.VERSION && checkVersionSent -> checkVersion(output)
            s == RESULTS.REMOTE_CHECK -> {
                checkImageInfo(output.trim().split(" "), mChatService.connectedDeviceName)
                listener.sendMessage(getString(R.string.TREEHOUSES_INTERNET))
                internetSent = true
            }
            s == RESULTS.BOOLEAN && internetSent -> checkPackage(output)
            else -> moreActions(output, s)
        }
    }

    private fun checkPackage(output: String) {
        internetSent = false
        if (output.contains("true")) internetstatus?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.circle_green))
        else internetstatus?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.circle_green))
        listener.sendMessage(getString(R.string.TREEHOUSES_UPGRADE_CHECK))
    }

    private fun moreActions(output: String, result: RESULTS) {
        when {
            result == RESULTS.UPGRADE_CHECK -> notificationListener?.setNotification(output.contains("true"))
            result == RESULTS.HOTSPOT_CONNECTED || result == RESULTS.WIFI_CONNECTED -> {
                updateStatus("Switched to $networkSsid")
            }
            result == RESULTS.BRIDGE_CONNECTED -> {
                updateStatus("Bridge Has Been Built")
            }
            result == RESULTS.DEFAULT_NETWORK -> switchProfile(networkProfile)
            result == RESULTS.ERROR -> {
                updateStatus("Network Not Found")
            }
            !testConnectionResult -> {
                testConnectionResult = true
                dismissTestConnection()
            }
        }
    }

    private fun updateStatus(message : String) {
        dismissPDialog()
        context.toast(message, Toast.LENGTH_LONG)
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    override fun getMessage(msg: Message) {
        when (msg.what) {
            Constants.MESSAGE_READ -> {
                val output = msg.obj as String
                if (output.isNotEmpty()) {
                    readMessage(output)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mChatService.state == Constants.STATE_CONNECTED) {
            mChatService.updateHandler(mHandler)
            checkVersionSent = true
            listener.sendMessage(getString(R.string.TREEHOUSES_REMOTE_VERSION, BuildConfig.VERSION_CODE))
        }
    }

    companion object {
        @JvmField
        val group_labels = arrayOf("WiFi", "Hotspot", "Bridge", "Default")
    }

}
