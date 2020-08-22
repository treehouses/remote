package io.treehouses.remote.ui.home

import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import io.treehouses.remote.*
import io.treehouses.remote.Constants.REQUEST_ENABLE_BT
import io.treehouses.remote.Fragments.AboutFragment
import io.treehouses.remote.Fragments.DialogFragments.BluetoothFailedDialog
import io.treehouses.remote.Fragments.DialogFragments.RPIDialogFragment
import io.treehouses.remote.Fragments.TerminalFragment
import io.treehouses.remote.InitialActivity.Companion.instance
import io.treehouses.remote.adapter.ProfilesListAdapter
import io.treehouses.remote.callback.NotificationCallback
import io.treehouses.remote.databinding.ActivityHomeFragmentBinding
import io.treehouses.remote.pojo.enum.Resource
import io.treehouses.remote.pojo.enum.Status
import io.treehouses.remote.utils.SaveUtils
import io.treehouses.remote.utils.Utils.toast

class HomeFragment : BaseHomeFragment() {
    private var notificationListener: NotificationCallback? = null
    private var progressDialog: ProgressDialog? = null
    private var testConnectionDialog: AlertDialog? = null
    private var connectionDialog: ProgressDialog? = null

    private lateinit var bind: ActivityHomeFragmentBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityHomeFragmentBinding.inflate(inflater, container, false)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        setupProfiles()
        showDialogOnce(preferences!!)
        connectRpiListener()
        testConnectionListener()
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeConnectionState()
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

        observers()
        errorConnecting()
        observeNetworkProfileSwitch()

    }

    private fun errorConnecting() {
        viewModel.errorConnecting.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            connectionDialog?.dismiss()
            val noDialog = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(BluetoothFailedDialog.DONT_SHOW_DIALOG, false)
            if (!noDialog) BluetoothFailedDialog().show(childFragmentManager, "ERROR")
            viewModel.errorConnecting.value = null
        })
    }

    private fun observers() {
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
                Status.SUCCESS, Status.ERROR, Status.NOTHING -> {
                    if (progressDialog != null) progressDialog!!.dismiss()
                    if (it.message.isNotEmpty()) context.toast(it.message)
                }
                Status.LOADING -> {
                    if (it == null) return@Observer
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
            if (viewModel.connectionStatus.value == Constants.STATE_CONNECTED) {
                RPIDialogFragment.instance!!.bluetoothCheck("unregister")
                viewModel.disconnectBT()
                return@setOnClickListener
            }
            if (mBluetoothAdapter?.state == BluetoothAdapter.STATE_OFF) {
                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
                context.toast( "Bluetooth is disabled", Toast.LENGTH_LONG)
            } else if (mBluetoothAdapter?.state == BluetoothAdapter.STATE_ON) {
                val dialogFrag = RPIDialogFragment.newInstance(123)
                dialogFrag.show(childFragmentManager.beginTransaction(), "rpiDialog")
            }
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

    private fun observeConnectionState() {
        viewModel.connectionStatus.observe(viewLifecycleOwner, androidx.lifecycle.Observer {connected ->
            Log.e("CONNECTED", "STATE $connected")
            transition(connected == Constants.STATE_CONNECTED)
            connectionDialog?.dismiss()
            when(connected) {
                Constants.STATE_CONNECTED -> {
                    showLogDialog(preferences!!)
                    viewModel.internetSent = true
                    viewModel.sendMessage("if nc -w 10 -z 8.8.8.8 53; then echo \"true\"; else echo \"false\"; fi\n")
//                    viewModel.sendMessage(getString(R.string.TREEHOUSES_INTERNET))
                    Tutorials.homeTutorials(bind, requireActivity())
                }
                Constants.STATE_CONNECTING -> {
                    showBTConnectionDialog()
                }
                else -> {
                    viewModel.hashSent.value = Resource.nothing()
                    (activity?.application as MainApplication).logSent = false
                }
            }
        })
        viewModel.loadBT()
    }

    private fun showBTConnectionDialog() {
        connectionDialog = ProgressDialog(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
        connectionDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        connectionDialog!!.setTitle("Connecting...")
        connectionDialog!!.setMessage("""
    Device Name: ${viewModel.device?.name}
    Device Address: ${viewModel.device?.address}
    """.trimIndent())
        connectionDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        connectionDialog!!.show()
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


    override fun onResume() {
        super.onResume()
        viewModel.refreshHandler()
        if (viewModel.connectionStatus.value == Constants.STATE_CONNECTED) {
            viewModel.checkVersionSent = true
            viewModel.sendMessage(getString(R.string.TREEHOUSES_REMOTE_VERSION, BuildConfig.VERSION_CODE))
        }
    }

    companion object {
        @JvmField
        val group_labels = arrayOf("WiFi", "Hotspot", "Bridge", "Default")
    }

}
