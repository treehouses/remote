package io.treehouses.remote.ui.home

import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.preference.PreferenceManager
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.treehouses.remote.*
import io.treehouses.remote.Constants.REQUEST_ENABLE_BT
import io.treehouses.remote.Fragments.AboutFragment
import io.treehouses.remote.Fragments.DialogFragments.RPIDialogFragment
import io.treehouses.remote.Fragments.TerminalFragment
import io.treehouses.remote.InitialActivity.Companion.instance
import io.treehouses.remote.adapter.ProfilesListAdapter
import io.treehouses.remote.callback.NotificationCallback
import io.treehouses.remote.callback.SetDisconnect
import io.treehouses.remote.databinding.ActivityHomeFragmentBinding
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.pojo.enum.Resource
import io.treehouses.remote.pojo.enum.Status
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.SaveUtils
import io.treehouses.remote.utils.Utils.toast
import io.treehouses.remote.utils.match
import java.util.*

class HomeFragment : BaseHomeFragment(), SetDisconnect {
    private var notificationListener: NotificationCallback? = null
    private var progressDialog: ProgressDialog? = null
    private var testConnectionDialog: AlertDialog? = null

    private lateinit var bind: ActivityHomeFragmentBinding
    private lateinit var viewModel : HomeViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityHomeFragmentBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        setupProfiles()
        showDialogOnce(preferences!!)
        checkConnectionState()
        connectRpiListener()
        testConnectionListener()
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind.btnGetStarted.setOnClickListener {
            instance!!.checkStatusNow()
            if (instance!!.hasValidConnection()) {
                switchFragment(TerminalFragment(), "Terminal")
            } else {
                switchFragment(AboutFragment(), "About")
            }
        }

        viewModel.error.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            showUpgradeCLI()
        })

        viewModel.remoteUpdateRequired.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            updateTreehousesRemote()
        })

        viewModel.newCLIUpgradeAvailable.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            notificationListener?.setNotification(it)
        })

        viewModel.internetStatus.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) bind.internetstatus.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.circle_green))
            else bind.internetstatus.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.circle_red))
        })

        viewModel.testConnectionResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.status == Status.SUCCESS) dismissTestConnection()
        })

        viewModel.hashSent.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.status == Status.SUCCESS) syncBluetooth(it.data ?: "error")
        })

    }

    private fun observeNetworkProfileSwitch() {
        viewModel.networkProfileResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            when(it.status) {
                Status.SUCCESS, Status.ERROR -> {
                    if (progressDialog != null) progressDialog!!.dismiss()
                    context.toast(it.message)
                }
                Status.LOADING -> {
                    progressDialog = ProgressDialog.show(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle), "Connecting...", "Switching to " + it.data?.ssid, true)
                    progressDialog?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                    progressDialog?.show()
                }
            }
        })
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
                viewModel.sendMessage(getString(R.string.TREEHOUSES_DEFAULT_NETWORK))
                context.toast("Switched to Default Network", Toast.LENGTH_LONG)
            } else if (SaveUtils.getProfiles(requireContext()).size > 0 && SaveUtils.getProfiles(requireContext())[listOf(*group_labels)[groupPosition]]!!.isNotEmpty()) {
                if (SaveUtils.getProfiles(requireContext())[listOf(*group_labels)[groupPosition]]!!.size <= childPosition) return@setOnChildClickListener false
                viewModel.networkProfile = SaveUtils.getProfiles(requireContext())[listOf(*group_labels)[groupPosition]]!![childPosition]
                viewModel.sendMessage(getString(R.string.TREEHOUSES_DEFAULT_NETWORK))
                requireContext().toast("Configuring...", Toast.LENGTH_LONG)
            }
            false
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
            if (viewModel.connected.value == true) {
                RPIDialogFragment.instance!!.bluetoothCheck("unregister")
                viewModel.disconnectBT()
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
            viewModel.selectedLed = options.indexOf(preference)
            viewModel.sendMessage(optionsCode[viewModel.selectedLed])
            testConnectionDialog = showTestConnectionDialog(false, "Testing Connection...", R.string.test_connection_message, viewModel.selectedLed)
            testConnectionDialog?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            testConnectionDialog?.show()
            viewModel.testConnectionResult.value = Resource.loading()
        }
    }

    override fun checkConnectionState() {
        viewModel.connected.observe(viewLifecycleOwner, androidx.lifecycle.Observer {connected ->
            transition(connected)
            if (connected) {
                showLogDialog(preferences!!)
                viewModel.sendMessage("remotehash")
                viewModel.hashSent.value = Resource.loading("")
                Tutorials.homeTutorials(bind, requireActivity())
            } else {
                viewModel.hashSent.value = Resource.nothing()
                (activity?.application as MainApplication).logSent = false
            }
        })
        viewModel.loadBT()
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
            showTestConnectionDialog(true, "Process Finished", R.string.test_finished, viewModel.selectedLed)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        notificationListener = try { getContext() as NotificationCallback?
        } catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement NotificationListener")
        }
    }


    private fun updateTreehousesRemote() {
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


    override fun onResume() {
        super.onResume()
        if (mChatService.state == Constants.STATE_CONNECTED) {
//            mChatService.updateHandler(mHandler)
            viewModel.loadBT()
            viewModel.checkVersionSent = true
            viewModel.sendMessage(getString(R.string.TREEHOUSES_REMOTE_VERSION, BuildConfig.VERSION_CODE))
//            listener.sendMessage(getString(R.string.TREEHOUSES_REMOTE_VERSION, BuildConfig.VERSION_CODE))
        }
    }

    companion object {
        @JvmField
        val group_labels = arrayOf("WiFi", "Hotspot", "Bridge", "Default")
    }

}
