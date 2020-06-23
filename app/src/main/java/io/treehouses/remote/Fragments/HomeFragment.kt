package io.treehouses.remote.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import kotlinx.android.synthetic.main.activity_home_fragment.*
import java.util.*

class HomeFragment : BaseHomeFragment(), SetDisconnect {
    private var notificationListener: NotificationCallback? = null
    private var progressDialog: ProgressDialog? = null
    private var connectionState = false
    private var testConnectionResult = false
    private var testConnectionDialog: AlertDialog? = null
    private var selected_LED = 0
    private var checkVersionSent = false
    private var internetSent = false
    private var network_ssid = ""
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
            instance!!.openCallFragment(AboutFragment())
            activity?.let { it.title = "About" }
        }
        testConnectionListener()
        return bind.root
    }

    private fun setupProfiles() {
        val profileAdapter = ProfilesListAdapter(context, listOf(*group_labels), SaveUtils.getProfiles(requireContext()))
        bind.networkProfiles.setAdapter(profileAdapter)
        bind.networkProfiles.setOnChildClickListener { _: ExpandableListView?, _: View?, groupPosition: Int, childPosition: Int, _: Long ->
            if (groupPosition == 3) {
                listener.sendMessage(getString(R.string.TREEHOUSES_DEFAULT_NETWORK))
                Toast.makeText(context, "Switched to Default Network", Toast.LENGTH_LONG).show()
            } else if (SaveUtils.getProfiles(requireContext()).size > 0 && SaveUtils.getProfiles(requireContext())[listOf(*group_labels)[groupPosition]]!!.size > 0) {
                if (SaveUtils.getProfiles(requireContext())[listOf(*group_labels)[groupPosition]]!!.size <= childPosition) return@setOnChildClickListener false
                networkProfile = SaveUtils.getProfiles(requireContext())[listOf(*group_labels)[groupPosition]]!![childPosition]
                listener.sendMessage(getString(R.string.TREEHOUSES_DEFAULT_NETWORK))
                Toast.makeText(requireContext(), "Configuring...", Toast.LENGTH_LONG).show()
            }
            false
        }
    }

    private fun switchProfile(profile: NetworkProfile?) {
        if (profile == null) return
        progressDialog = ProgressDialog.show(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle), "Connecting...", "Switching to " + profile.ssid, true)
        progressDialog?.show()
        when {
            profile.isWifi -> {
                //WIFI
                listener.sendMessage(
                        getString(if (profile.isHidden) R.string.TREEHOUSES_WIFI_HIDDEN else R.string.TREEHOUSES_WIFI,
                        profile.ssid, profile.password))
                network_ssid = profile.ssid
            }
            profile.isHotspot -> {
                //Hotspot
                listener.sendMessage(
                        getString(if (profile.isHidden) R.string.TREEHOUSES_AP_HIDDEN else R.string.TREEHOUSES_AP,
                        profile.option, profile.ssid, profile.password))
                network_ssid = profile.ssid
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
    }

    private fun connectRpiListener() {
        bind.btnConnect.setOnClickListener {
            val vibe = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibe.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibe.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                else vibe.vibrate(10)
            }
            if (connectionState) {
                RPIDialogFragment.getInstance().bluetoothCheck("unregister")
                mChatService.stop()
                connectionState = false
                checkConnectionState()
                return@setOnClickListener
            }
            if (mBluetoothAdapter?.state == BluetoothAdapter.STATE_OFF) {
                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
                Toast.makeText(context, "Bluetooth is disabled", Toast.LENGTH_LONG).show()
            } else if (mBluetoothAdapter?.state == BluetoothAdapter.STATE_ON) showRPIDialog(this@HomeFragment)
        }
    }

    private fun testConnectionListener() {
        bind.testConnection.setOnClickListener {
            val preference = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getString("led_pattern", "LED Heavy Metal")
            val options = listOf(*resources.getStringArray(R.array.led_options))
            val optionsCode = resources.getStringArray(R.array.led_options_commands)
            selected_LED = options.indexOf(preference)
            listener.sendMessage(optionsCode[selected_LED])
            testConnectionDialog = showTestConnectionDialog(false, "Testing Connection...", R.string.test_connection_message, selected_LED)
            testConnectionDialog?.show()
            testConnectionResult = false
        }
    }

    override fun checkConnectionState() {
        mChatService = listener.getChatService()
        if (mChatService.state == Constants.STATE_CONNECTED) {
            showLogDialog(preferences!!)
            transition(true, arrayOf(150f, 110f, 70f))
            connectionState = true
            checkVersionSent = true
            listener.sendMessage(getString(R.string.TREEHOUSES_REMOTE_VERSION, BuildConfig.VERSION_CODE))
            Tutorials.homeTutorials(bind, requireActivity())
        } else {
            transition(false, arrayOf(0f, 0f, 0f))
            connectionState = false
            MainApplication.logSent = false
        }
        mChatService.updateHandler(mHandler)
    }

    private fun transition(connected: Boolean, values: Array<Float>) {
        bind.btnConnect.text = if (connected) "Disconnect" else "Connect to RPI"
        bind.btnConnect.setBackgroundResource(if (connected) R.drawable.ic_disconnect_rpi else R.drawable.ic_connect_to_rpi)
        bind.backgroundHome.animate().translationY(values[0])
        bind.btnConnect.animate().translationY(values[1])
        bind.btnGetStarted.animate().translationY(values[2])
        val b1 = if (connected) View.GONE else View.VISIBLE     //Show on Boot
        val b2 = if (connected) View.VISIBLE else View.GONE     //Show when connected
        bind.welcomeHome.visibility = b1
        bind.logoHome.visibility = b1
        bind.testConnection.visibility = b2
        bind.layoutBack.visibility = b2

    }

    private fun dismissTestConnection() {
        if (testConnectionDialog != null) {
            testConnectionDialog!!.cancel()
            showTestConnectionDialog(true, "Process Finished", R.string.test_finished, selected_LED)
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
            alertDialog.show()

        }
    }

    private fun readMessage(output: String) {
        notificationListener = try { context as NotificationCallback?
        } catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement NotificationListener")
        }
        val s = match(output)
        when {
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
            s == RESULTS.BOOLEAN && internetSent -> {
                internetSent = false
                if (output.contains("true")) internetstatus?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.circle_green))
                else internetstatus?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.circle_green))
                listener.sendMessage(getString(R.string.TREEHOUSES_UPGRADE_CHECK))
            }
            else -> moreActions(output, s)
        }
    }

    private fun moreActions(output: String, result: RESULTS) {
        when {
            result == RESULTS.UPGRADE_CHECK -> notificationListener?.setNotification(output.contains("true"))
            result == RESULTS.HOTSPOT_CONNECTED || result == RESULTS.WIFI_CONNECTED -> {
                dismissPDialog()
                Toast.makeText(context, "Switched to $network_ssid", Toast.LENGTH_LONG).show()
            }
            result == RESULTS.BRIDGE_CONNECTED -> {
                dismissPDialog()
                Toast.makeText(context, "Bridge Has Been Built", Toast.LENGTH_LONG).show()
            }
            result == RESULTS.DEFAULT_NETWORK -> switchProfile(networkProfile)
            result == RESULTS.ERROR -> {
                dismissPDialog()
                Toast.makeText(context, "Network Not Found", Toast.LENGTH_LONG).show()
            }
            !testConnectionResult -> {
                testConnectionResult = true
                dismissTestConnection()
            }
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    val output = msg.obj as String
                    if (output.isNotEmpty()) {
                        readMessage(output)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mChatService.state == Constants.STATE_CONNECTED) {
            checkVersionSent = true
            listener.sendMessage(getString(R.string.TREEHOUSES_REMOTE_VERSION, BuildConfig.VERSION_CODE))
        }
    }

    companion object {
        @JvmField
        val group_labels = arrayOf("WiFi", "Hotspot", "Bridge", "Default")
    }
}