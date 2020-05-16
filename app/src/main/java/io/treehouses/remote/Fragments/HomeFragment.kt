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
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.Toast
import io.treehouses.remote.BuildConfig
import io.treehouses.remote.Constants
import io.treehouses.remote.Constants.REQUEST_ENABLE_BT
import io.treehouses.remote.Fragments.DialogFragments.RPIDialogFragment
import io.treehouses.remote.InitialActivity.Companion.instance
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.adapter.ProfilesListAdapter
import io.treehouses.remote.bases.BaseHomeFragment
import io.treehouses.remote.callback.NotificationCallback
import io.treehouses.remote.callback.SetDisconnect
import io.treehouses.remote.databinding.ActivityHomeFragmentBinding
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.utils.SaveUtils
import kotlinx.android.synthetic.main.activity_home_fragment.*
import java.util.*

class HomeFragment : BaseHomeFragment(), SetDisconnect {
    private var notificationListener: NotificationCallback? = null
    private var progressDialog: ProgressDialog? = null
    private var connectionState = false
    private var result = false
    private var testConnectionDialog: AlertDialog? = null
    private var selected_LED = 0
    private var checkVersionSent = false
    private var internetSent = false
    private var network_ssid = ""
    private var networkProfile: NetworkProfile? = null

    private lateinit var bind: ActivityHomeFragmentBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityHomeFragmentBinding.inflate(inflater, container, false)
        mChatService = listener.chatService
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
        val profileAdapter = ProfilesListAdapter(context, listOf(*group_labels), SaveUtils.getProfiles(context))
        bind.networkProfiles.setAdapter(profileAdapter)
        bind.networkProfiles.setOnChildClickListener { _: ExpandableListView?, _: View?, groupPosition: Int, childPosition: Int, _: Long ->
            if (groupPosition == 3) {
                listener.sendMessage("treehouses default network")
                Toast.makeText(context, "Switched to Default Network", Toast.LENGTH_LONG).show()
            } else if (SaveUtils.getProfiles(context).size > 0 && SaveUtils.getProfiles(context)[listOf(*group_labels)[groupPosition]]!!.size > 0) {
                if (SaveUtils.getProfiles(context)[listOf(*group_labels)[groupPosition]]!!.size <= childPosition) return@setOnChildClickListener false
                networkProfile = SaveUtils.getProfiles(context)[listOf(*group_labels)[groupPosition]]!![childPosition]
                listener.sendMessage("treehouses default network \n")
                Toast.makeText(context, "Configuring...", Toast.LENGTH_LONG).show()
            }
            false
        }
    }

    private fun switchProfile(networkProfile: NetworkProfile?) {
        if (networkProfile == null) return
        progressDialog = ProgressDialog.show(context, "Connecting...", "Switching to " + networkProfile.ssid, true)
        progressDialog?.show()
        when {
            networkProfile.isWifi -> {
                //WIFI
                listener.sendMessage(String.format("treehouses wifi %s %s", networkProfile.ssid, networkProfile.password))
                network_ssid = networkProfile.ssid
            }
            networkProfile.isHotspot -> {
                //Hotspot
                if (networkProfile.password.isEmpty()) listener.sendMessage("treehouses ap " + networkProfile.option + " " + networkProfile.ssid)
                else listener.sendMessage("treehouses ap " + networkProfile.option + " " + networkProfile.ssid + " " + networkProfile.password)
                network_ssid = networkProfile.ssid
            }
            networkProfile.isBridge -> {
                //Bridge
                val temp = "treehouses bridge " + networkProfile.ssid + " " + networkProfile.hotspot_ssid + " "
                var overallMessage = if (TextUtils.isEmpty(networkProfile.password)) "$temp " else temp + " " + networkProfile.password
                if (!TextUtils.isEmpty(networkProfile.hotspot_password)) overallMessage += " " + networkProfile.hotspot_password + " "
                listener.sendMessage(overallMessage)
            }
            else -> {
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
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                Toast.makeText(context, "Bluetooth is disabled", Toast.LENGTH_LONG).show()
            } else if (mBluetoothAdapter?.state == BluetoothAdapter.STATE_ON) {
                showRPIDialog(this@HomeFragment)
            }
        }
    }

    fun testConnectionListener() {
        bind.testConnection.setOnClickListener {
            val preference = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getString("led_pattern", "LED Heavy Metal")
            val options = listOf(*resources.getStringArray(R.array.led_options))
            val optionsCode = resources.getStringArray(R.array.led_options_commands)
            selected_LED = options.indexOf(preference)
            writeToRPI(optionsCode[selected_LED])
            testConnectionDialog = showTestConnectionDialog(false, "Testing Connection...", R.string.test_connection_message, selected_LED)
            testConnectionDialog?.show()
            result = false
        }
    }

    override fun checkConnectionState() {
        mChatService = listener.chatService
        if (mChatService.state == Constants.STATE_CONNECTED) {
            showLogDialog(preferences!!)
            transitionOnConnected()
            connectionState = true
            checkVersionSent = true
            writeToRPI("""treehouses remote version ${BuildConfig.VERSION_CODE}""".trimIndent())
        } else {
            transitionDisconnected()
            connectionState = false
        }
        mChatService.updateHandler(mHandler)
    }

    private fun transitionOnConnected() {
        bind.welcomeHome.visibility = View.GONE
        bind.testConnection.visibility = View.VISIBLE
        bind.btnConnect.text = "Disconnect"
        bind.btnConnect.setBackgroundResource(R.drawable.ic_disconnect_rpi)
        bind.backgroundHome.animate().translationY(150f)
        bind.btnConnect.animate().translationY(110f)
        bind.btnGetStarted.animate().translationY(70f)
        bind.testConnection.visibility = View.VISIBLE
        bind.layoutBack.visibility = View.VISIBLE
        bind.logoHome.visibility = View.GONE
    }

    private fun transitionDisconnected() {
        bind.btnConnect.text = "Connect to RPI"
        bind.testConnection.visibility = View.GONE
        bind.welcomeHome.visibility = View.VISIBLE
        bind.backgroundHome.animate().translationY(0f)
        bind.btnConnect.animate().translationY(0f)
        bind.btnGetStarted.animate().translationY(0f)
        bind.btnConnect.setBackgroundResource(R.drawable.ic_connect_to_rpi)
        bind.logoHome.visibility = View.VISIBLE
        bind.layoutBack.visibility = View.GONE
    }

    private fun dismissTestConnection() {
        if (testConnectionDialog != null) {
            testConnectionDialog!!.cancel()
            showTestConnectionDialog(true, "Process Finished", R.string.test_finished, selected_LED)
        }
    }

    private fun writeToRPI(ping: String) {
        mChatService.write(ping.toByteArray())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        notificationListener = try {
            getContext() as NotificationCallback?
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
            //CLI Needs Upgrade
            showUpgradeCLI()
        } else if (BuildConfig.VERSION_CODE == 2 || output.contains("true")) {
            writeToRPI("treehouses remote check\n")
        } else if (output.contains("false")) {
            val alertDialog = AlertDialog.Builder(context)
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
        notificationListener = try {
            context as NotificationCallback?
        } catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement NotificationListener")
        }
        //Remove in 1 month ( May 4th)
        if (output.contains("unknown")) {
            showUpgradeCLI()
            internetSent = false
        } else if (output.startsWith("version: ") || checkVersionSent) {
            checkVersion(output)
        } else if (output.contains(" ") && output.trim().split(" ").size == 4 && !matchResult(output, "pirateship", "bridge") && !output.contains("network")) {
            checkImageInfo(output.trim().split(" "), mChatService.connectedDeviceName)
            listener.sendMessage("treehouses internet\n")
            internetSent = true
        } else if (internetSent) {
            internetSent = false
            if (output.trim { it <= ' ' }.contains("true")) internetstatus!!.setImageDrawable(resources.getDrawable(R.drawable.circle_green)) else internetstatus!!.setImageDrawable(resources.getDrawable(R.drawable.circle))
            writeToRPI("treehouses upgrade --check\n")
        } else {
            moreActions(output)
        }
    }

    private fun moreActions(output: String) {
        if (notificationListener != null && matchResult(output, "true", "false") && output.length < 14) {
            notificationListener!!.setNotification(output.contains("true"))
        } else if (matchResult(output, "connected", "pirateship")) {
            Toast.makeText(context, "Switched to $network_ssid", Toast.LENGTH_LONG).show()
            dismissPDialog()
        } else if (output.toLowerCase(Locale.ROOT).contains("bridge has been built")) {
            dismissPDialog()
            Toast.makeText(context, "Bridge Has Been Built", Toast.LENGTH_LONG).show()
        } else if (output.toLowerCase(Locale.ROOT).contains("default")) {
            switchProfile(networkProfile)
        } else if (output.toLowerCase(Locale.ROOT).contains("error")) {
            dismissPDialog()
            Toast.makeText(context, "Network Not Found", Toast.LENGTH_LONG).show()
        } else if (!result) {
            //Test Connection
            result = true
            dismissTestConnection()
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    val output = msg.obj as String
                    if (!output.isEmpty()) {
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
            writeToRPI("""
    treehouses remote version ${BuildConfig.VERSION_CODE}

    """.trimIndent())
        }
    }

    companion object {
        @JvmField
        val group_labels = arrayOf("WiFi", "Hotspot", "Bridge", "Default")
    }
}