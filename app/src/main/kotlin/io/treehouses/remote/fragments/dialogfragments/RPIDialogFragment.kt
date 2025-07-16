package io.treehouses.remote.fragments.dialogfragments

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import io.treehouses.remote.R
import io.treehouses.remote.adapter.RPIListAdapter
import io.treehouses.remote.bases.BaseDialogFragment
import io.treehouses.remote.callback.DeviceDeleteListener
import io.treehouses.remote.databinding.ActivityRpiDialogFragmentBinding
import io.treehouses.remote.pojo.DeviceInfo
import io.treehouses.remote.ui.home.HomeViewModel
import io.treehouses.remote.utils.DialogUtils
import io.treehouses.remote.utils.DialogUtils.CustomProgressDialog

class RPIDialogFragment : BaseDialogFragment(), DeviceDeleteListener {
    private val raspberryDevices: MutableList<BluetoothDevice> = ArrayList()
    private val allDevices: MutableList<BluetoothDevice> = ArrayList()
    private var pairedDevices: Set<BluetoothDevice>? = null
    private var mArrayAdapter: ArrayAdapter<*>? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mDialog: AlertDialog? = null
    private val raspberryDevicesText: MutableList<DeviceInfo> = ArrayList()
    private val allDevicesText: MutableList<DeviceInfo> = ArrayList()
    private var pDialog: CustomProgressDialog? = null
    private var discoveryTimeoutHandler: Handler? = null
    private var discoveryTimeoutRunnable: Runnable? = null
    private var isDiscovering = false
    private var isReceiverRegistered = false

    private val viewModel: HomeViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private var bind: ActivityRpiDialogFragmentBinding? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        instance = this
        val bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        mBluetoothAdapter = bluetoothManager?.adapter
        bluetoothCheck()
        
        // Check location services before creating main dialog
        if (!checkLocationServices()) {
            // Return a dummy dialog that will be replaced by location rationale
            return AlertDialog.Builder(requireContext()).create()
        }
        
        startBluetoothDiscovery()
        bind = ActivityRpiDialogFragmentBinding.inflate(requireActivity().layoutInflater)
        initDialog()
        setAdapterNotNull(raspberryDevicesText)
        loadPairedDevices()
        intentFilter()
        mDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        return mDialog!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeConnectionStatus()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions granted, load paired devices and start discovery
                loadPairedDevices()
                startBluetoothDiscovery()
            } else {
                // Permissions denied, show message
                Toast.makeText(requireContext(), "Bluetooth and location permissions are required to scan for devices", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeConnectionStatus() {
//        viewModel.connectionStatus.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
//            when(it) {
//                Constants.STATE_CONNECTED -> {
//                    Log.e("RPIDialogFragment", "Bluetooth Connection Status Change: State Listen")
//                    pDialog?.dismiss()
//                    mBluetoothAdapter?.cancelDiscovery()
//                    Toast.makeText(requireContext(), "Bluetooth Connected", Toast.LENGTH_LONG).show()
//                }
//                Constants.STATE_CONNECTING -> {
//                    Log.e("HEREERER", "NICEE")
//                    pDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
//                    pDialog!!.setTitle("Connecting...")
//                    pDialog!!.setMessage("""
//    Device Name: ${mainDevice!!.name}
//    Device Address: ${mainDevice!!.address}
//    """.trimIndent())
//                    pDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
//                    pDialog!!.show()
//                }
//                Constants.STATE_NONE -> {
//                    pDialog?.dismiss()
//                    Toast.makeText(requireContext(), "Connection Failed: Please Try Again", Toast.LENGTH_LONG).show()
//                    Log.e("RPIDialogFragment", "Bluetooth Connection Status Change: State None")
//                }
//            }
//        })
    }

    private fun initDialog() {
        pDialog = CustomProgressDialog(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
        mDialog = getAlertDialog(bind!!.root)
        mDialog!!.setTitle(R.string.select_device)
        listViewOnClickListener(bind!!.listView)
        bind!!.rpiCloseButton.setOnClickListener {
            bluetoothCheck("unregister")
            dismiss()
        }
        bind!!.rpiSwitch.isChecked = true
        switchViewOnClickListener()
    }

    private fun intentFilter() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
            requireActivity().registerReceiver(mReceiver, filter)
            isReceiverRegistered = true
            Log.d("RPIDialogFragment", "Broadcast receiver registered")
        }
    }

    private fun listViewOnClickListener(mView: View) {
        bind!!.listView.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val deviceList: List<BluetoothDevice> = if (bind!!.rpiSwitch.isChecked) raspberryDevices else allDevices
            viewModel.connect(deviceList[position])
            mDialog!!.cancel()
            finish(mView)
        }
    }


    private fun switchViewOnClickListener() {
        bind!!.rpiSwitch.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                setAdapterNotNull(raspberryDevicesText)
                buttonView.setText(R.string.paired_devices)
                if (raspberryDevices.isEmpty()) {
                    bind!!.progressBar.visibility = View.VISIBLE
                    // Trigger a new scan when switching to Raspberry Pi devices
                    if (!isDiscovering) {
                        startBluetoothDiscovery()
                    }
                }
            } else {
                setAdapterNotNull(allDevicesText)
                buttonView.setText(R.string.all_devices)
                // Trigger a new scan when switching to all devices
                bind!!.progressBar.visibility = View.VISIBLE
                if (!isDiscovering) {
                    startBluetoothDiscovery()
                }
            }
        }
    }


    private fun finish(mView: View) {
        val mDialog = getAlertDialog(mView)
        mDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
//        if (status == 3) mDialog.setTitle("BLUETOOTH IS CONNECTED") else if (status == 2) mDialog.setTitle("BLUETOOTH IS CONNECTING...") else mDialog.setTitle("BLUETOOTH IS NOT CONNECTED")
        setAdapterNotNull(ArrayList())
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop discovery and cleanup
        stopBluetoothDiscovery()
        
        // Only unregister if it was registered
        if (isReceiverRegistered) {
            try {
                requireContext().unregisterReceiver(mReceiver)
                isReceiverRegistered = false
                Log.d("RPIDialogFragment", "Broadcast receiver unregistered")
            } catch (e: Exception) {
                Log.e("RPIDialogFragment", "Error unregistering receiver", e)
            }
        }
    }

    private fun getAlertDialog(mView: View): AlertDialog {
        return DialogUtils.createAlertDialog(context, mView, R.drawable.dialog_icon).create()
    }

    private fun checkLocationServices(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
                               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        if (!isLocationEnabled) {
            // Show location rationale dialog immediately
            showLocationServiceRationale()
            return false
        }
        return true
    }

    private fun showLocationServiceRationale() {
        // Capture context at creation time
        val savedContext = requireContext()
        
        // Post to main thread to ensure dialog is shown after fragment is fully initialized
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            val dialog = AlertDialog.Builder(savedContext)
                .setTitle("Location Services Required")
                .setMessage("To scan for Bluetooth devices, Android requires location services to be enabled. This is a system requirement and your location data is not collected by this app.\n\nWould you like to enable location services now?")
                .setIcon(R.drawable.dialog_icon)
                .setPositiveButton("Enable") { dialog, _ ->
                    Log.d("RPIDialogFragment", "Enable button clicked")
                    
                    // Dismiss dialog first to prevent multiple clicks
                    dialog.dismiss()
                    
                    // Post the intent opening to avoid any timing issues
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        openLocationSettings(savedContext)
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    Log.d("RPIDialogFragment", "Cancel button clicked")
                    dialog.dismiss()
                    Toast.makeText(savedContext, "Location services are required to connect to Bluetooth devices. Please enable location services to scan for devices.", Toast.LENGTH_LONG).show()
                }
                .setCancelable(false)
                .create()
            
            // Ensure dialog is fully focused and enabled
            dialog.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            dialog.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            
            dialog.show()
            
            // Ensure buttons are enabled after dialog is shown
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.isEnabled = true
        }
    }
    
    private fun openLocationSettings(context: Context) {
        var settingsOpened = false
        
        // Try primary location settings intent
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            settingsOpened = true
            Log.d("RPIDialogFragment", "Opened location settings with ACTION_LOCATION_SOURCE_SETTINGS")
        } catch (e: Exception) {
            Log.w("RPIDialogFragment", "Failed to open location settings with ACTION_LOCATION_SOURCE_SETTINGS", e)
        }
        
        // Try fallback to general settings if primary failed
        if (!settingsOpened) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                settingsOpened = true
                Log.d("RPIDialogFragment", "Opened general settings as fallback")
            } catch (e: Exception) {
                Log.e("RPIDialogFragment", "Failed to open any settings", e)
            }
        }
        
        if (!settingsOpened) {
            Toast.makeText(context, "Please manually enable location services in Settings > Location", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadPairedDevices() {
        try {
            pairedDevices = mBluetoothAdapter?.bondedDevices
            pairedDevices?.let { devices ->
                for (d in devices) {
                    // Add all paired devices to the allDevices list
                    addToDialog(d, allDevicesText, allDevices, false)
                    
                    // Add Raspberry Pi devices to the raspberryDevices list
                    if (checkPiAddress(d.address)) {
                        addToDialog(d, raspberryDevicesText, raspberryDevices, false)
                        bind!!.progressBar.visibility = View.INVISIBLE
                    }
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Bluetooth permissions are required to access paired devices", Toast.LENGTH_LONG).show()
        }
    }

    private fun startBluetoothDiscovery() {
        // Check if we have the required permissions based on Android version
        val permissionsToRequest = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requires BLUETOOTH_SCAN and BLUETOOTH_CONNECT
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            // For older versions, check BLUETOOTH and BLUETOOTH_ADMIN
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH)
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
        
        // Location permissions are always required for Bluetooth scanning
        val fineLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
        
        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED && 
            coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            // Request permissions
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray(),
                REQUEST_BLUETOOTH_PERMISSIONS
            )
            return
        }
        
        // We have permissions, proceed with discovery
        try {
            // Check bluetooth adapter state
            if (mBluetoothAdapter == null) {
                Log.e("RPIDialogFragment", "Bluetooth adapter is null")
                bind?.progressBar?.visibility = View.INVISIBLE
                return
            }
            
            if (!mBluetoothAdapter!!.isEnabled) {
                Log.e("RPIDialogFragment", "Bluetooth is not enabled")
                Toast.makeText(requireContext(), "Please enable Bluetooth to scan for devices", Toast.LENGTH_LONG).show()
                bind?.progressBar?.visibility = View.INVISIBLE
                return
            }
            
            Log.d("RPIDialogFragment", "Bluetooth adapter is enabled, checking discovery state")
            
            if (mBluetoothAdapter!!.isDiscovering) {
                Log.d("RPIDialogFragment", "Discovery already running, cancelling first")
                mBluetoothAdapter!!.cancelDiscovery()
                // Wait a bit for cancellation to complete
                Thread.sleep(100)
            }
            
            // Cancel any existing timeout
            discoveryTimeoutRunnable?.let { discoveryTimeoutHandler?.removeCallbacks(it) }
            
            // Double-check permissions right before starting discovery
            val bluetoothScanCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
            } else {
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADMIN)
            }
            
            val locationCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            
            Log.d("RPIDialogFragment", "Permission check before discovery - BLUETOOTH_SCAN/ADMIN: $bluetoothScanCheck, LOCATION: $locationCheck")
            
            if (bluetoothScanCheck != PackageManager.PERMISSION_GRANTED || locationCheck != PackageManager.PERMISSION_GRANTED) {
                Log.e("RPIDialogFragment", "Permissions not granted right before discovery")
                Toast.makeText(requireContext(), "Bluetooth and location permissions are required", Toast.LENGTH_LONG).show()
                bind?.progressBar?.visibility = View.INVISIBLE
                return
            }
            
            // Start discovery
            Log.d("RPIDialogFragment", "Attempting to start discovery")
            val discoveryStarted = mBluetoothAdapter!!.startDiscovery()
            Log.d("RPIDialogFragment", "Discovery started: $discoveryStarted")
            
            if (discoveryStarted) {
                isDiscovering = true
                // Set up timeout handler (10 seconds)
                discoveryTimeoutHandler = Handler(Looper.getMainLooper())
                discoveryTimeoutRunnable = Runnable {
                    Log.d("RPIDialogFragment", "Discovery timeout reached")
                    if (isDiscovering) {
                        stopBluetoothDiscovery()
                    }
                }
                discoveryTimeoutRunnable?.let { 
                    Log.d("RPIDialogFragment", "Setting timeout for 10 seconds")
                    discoveryTimeoutHandler?.postDelayed(it, 10000) 
                }
            } else {
                Log.e("RPIDialogFragment", "Failed to start discovery - bluetooth adapter returned false")
                bind?.progressBar?.visibility = View.INVISIBLE
                Toast.makeText(requireContext(), "Failed to start device scan", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Log.e("RPIDialogFragment", "Security exception starting discovery", e)
            Toast.makeText(requireContext(), "Bluetooth permissions are required for device discovery", Toast.LENGTH_LONG).show()
            bind?.progressBar?.visibility = View.INVISIBLE
        } catch (e: InterruptedException) {
            Log.e("RPIDialogFragment", "Interrupted while waiting for discovery cancellation", e)
            bind?.progressBar?.visibility = View.INVISIBLE
        }
    }

    private fun stopBluetoothDiscovery() {
        Log.d("RPIDialogFragment", "Stopping discovery")
        try {
            if (mBluetoothAdapter?.isDiscovering == true) {
                mBluetoothAdapter?.cancelDiscovery()
                Log.d("RPIDialogFragment", "Discovery cancelled")
            }
        } catch (e: SecurityException) {
            Log.e("RPIDialogFragment", "Permission error stopping discovery", e)
        }
        isDiscovering = false
        discoveryTimeoutRunnable?.let { discoveryTimeoutHandler?.removeCallbacks(it) }
        bind?.progressBar?.visibility = View.INVISIBLE
        Log.d("RPIDialogFragment", "Progress bar hidden")
    }

    fun bluetoothCheck(vararg args: String) {
        if (mBluetoothAdapter == null) {
            Toast.makeText(activity, "Your Bluetooth Is Not Enabled or Not Supported", Toast.LENGTH_LONG).show()
            dismiss()
//            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, requireActivity().intent)
            try {
                requireContext().unregisterReceiver(mReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (args.isNotEmpty() && args[0] == "unregister") {
            try {
                requireContext().unregisterReceiver(mReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
//            val intent = Intent()
//            intent.putExtra("mChatService", mChatService)
//            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        }
    }

    private fun setAdapterNotNull(listVal: List<DeviceInfo>) {
        mArrayAdapter = RPIListAdapter(requireContext(), listVal)
        (mArrayAdapter as RPIListAdapter).deviceListener = this
        bind!!.listView.adapter = mArrayAdapter
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("RPIDialogFragment", "Broadcast received: ${intent.action}")
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = try {
                        device?.name ?: "Unknown"
                    } catch (e: SecurityException) {
                        "Unknown"
                    }
                    Log.d("RPIDialogFragment", "Device found: $deviceName - ${device?.address}")
                    if (checkPiAddress(device!!.address)) {
                        addToDialog(device, raspberryDevicesText, raspberryDevices, true)
                        // Don't hide progress bar immediately - let timeout handle it
                    }
                    addToDialog(device, allDevicesText, allDevices, true)
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    // Discovery started, show progress bar
                    Log.d("RPIDialogFragment", "Discovery started broadcast received")
                    isDiscovering = true
                    bind?.progressBar?.visibility = View.VISIBLE
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    // Discovery has finished, stop everything
                    Log.d("RPIDialogFragment", "Discovery finished broadcast received")
                    stopBluetoothDiscovery()
                }
            }
        }
    }

    private fun addToDialog(device: BluetoothDevice, textList: MutableList<DeviceInfo>, mDevices: MutableList<BluetoothDevice>, inRange: Boolean) {
        if (!mDevices.contains(device)) {
            mDevices.add(device)
            val deviceName = try {
                device.name ?: "Unknown Device"
            } catch (e: SecurityException) {
                "Unknown Device"
            }
            val isPaired = pairedDevices?.contains(device) ?: false
            textList.add(DeviceInfo("$deviceName\n ${device.address}".trimIndent(), isPaired, inRange))
        } else textList[mDevices.indexOf(device)].isInRange = true
        mArrayAdapter?.notifyDataSetChanged()
    }


    companion object {
        var instance: RPIDialogFragment? = null
            private set
        private var mainDevice: BluetoothDevice? = null
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 1001
        fun newInstance(num: Int): DialogFragment {
            val rpiDialogFragment = RPIDialogFragment()
            val bundle = Bundle()
            bundle.putInt("num", num)
            rpiDialogFragment.arguments = bundle
            return rpiDialogFragment
        }

        fun checkPiAddress(deviceHardwareAddress: String): Boolean {
            val piAddress: Set<String> = HashSet(listOf(
                "B8:27:EB", "DC:A6:32", "E4:5F:01", "28:CD:C1", "D8:3A:DD", "2C:CF:67",
                "B8-27-EB", "DC-A6-32", "E4-5F-01", "28-CD-C1", "D8-3A-DD", "2C-CF-67",
                "B827.EB", "DCA6.32", "E45F.01", "28CD.C1", "D83A.DD", "2CCF.67",
                "b8:27:eb", "dc:a6:32", "e4:5f:01", "28:cd:c1", "d8:3a:dd", "2c:cf:67",
                "b8-27-eb", "dc-a6-32", "e4-5f-01", "28-cd-c1", "d8-3a-dd", "2c-cf-67",
                "b827.eb", "dca6.32", "e45f.01", "28cd.c1", "d83a.dd", "2ccf.67"))
            for (item in piAddress) {
                if (deviceHardwareAddress.contains(item)) return true
            }
            return false
        }

    }

    override fun onDeviceDeleted(position: Int) {
        var device = raspberryDevices[position]
        AlertDialog.Builder(activity).setMessage(R.string.delete_device_message)
                .setPositiveButton("Yes") { _, _ ->
                    try {
                        device::class.java.getMethod("removeBond").invoke(device)
                        raspberryDevices.removeAt(position)
                        raspberryDevicesText.removeAt(position)
                        allDevices.remove(device)
                        mArrayAdapter?.notifyDataSetChanged()
                    } catch (e: Exception) {
                        Toast.makeText(activity, "Unable to delete device", Toast.LENGTH_LONG).show()
                    }
                }.setNegativeButton("No", null).show()

    }
}
