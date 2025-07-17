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

        if (!checkLocationServices()) {
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
                loadPairedDevices()
                startBluetoothDiscovery()
            } else {
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
                    if (!isDiscovering) {
                        startBluetoothDiscovery()
                    }
                }
            } else {
                setAdapterNotNull(allDevicesText)
                buttonView.setText(R.string.all_devices)
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
        stopBluetoothDiscovery()

        if (isReceiverRegistered) {
            try {
                requireContext().unregisterReceiver(mReceiver)
                isReceiverRegistered = false
            } catch (e: Exception) {
                e.printStackTrace()
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
            showLocationServiceRationale()
            return false
        }
        return true
    }

    private fun showLocationServiceRationale() {
        val savedContext = ContextThemeWrapper(context, R.style.CustomAlertDialogStyle)

        Handler(Looper.getMainLooper()).post {
            val dialog = AlertDialog.Builder(savedContext)
                .setTitle("Location Services Required")
                .setMessage("To scan for Bluetooth devices, Android requires location services to be enabled. This is a system requirement and your location data is not collected by this app.\n\nWould you like to enable location services now?")
                .setIcon(R.drawable.dialog_icon)
                .setPositiveButton("Enable") { dialog, _ ->
                    dialog.dismiss()

                    Handler(Looper.getMainLooper()).post {
                        openLocationSettings(savedContext)
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(savedContext, "Location services are required to connect to Bluetooth devices. Please enable location services to scan for devices.", Toast.LENGTH_LONG).show()
                }
                .setCancelable(false)
                .create()

            dialog.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            dialog.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.isEnabled = true
        }
    }
    
    private fun openLocationSettings(context: Context) {
        var settingsOpened = false

        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            settingsOpened = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!settingsOpened) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                settingsOpened = true
            } catch (e: Exception) {
                e.printStackTrace()
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
                    addToDialog(d, allDevicesText, allDevices, false)

                    if (checkPiAddress(d.address)) {
                        addToDialog(d, raspberryDevicesText, raspberryDevices, false)
                        bind!!.progressBar.visibility = View.INVISIBLE
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Bluetooth permissions are required to access paired devices", Toast.LENGTH_LONG).show()
        }
    }

    private fun startBluetoothDiscovery() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH)
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }

        // Only request coarse location (no GPS) - required for Bluetooth scanning
        val coarseLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
        
        if (coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), permissionsToRequest.toTypedArray(), REQUEST_BLUETOOTH_PERMISSIONS)
            return
        }

        try {
            if (mBluetoothAdapter == null) {
                bind?.progressBar?.visibility = View.INVISIBLE
                return
            }
            
            if (!mBluetoothAdapter!!.isEnabled) {
                Toast.makeText(requireContext(), "Please enable Bluetooth to scan for devices", Toast.LENGTH_LONG).show()
                bind?.progressBar?.visibility = View.INVISIBLE
                return
            }
            
            if (mBluetoothAdapter!!.isDiscovering) {
                mBluetoothAdapter!!.cancelDiscovery()
                Thread.sleep(100)
            }

            discoveryTimeoutRunnable?.let { discoveryTimeoutHandler?.removeCallbacks(it) }

            val bluetoothScanCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
            } else {
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADMIN)
            }
            
            val locationCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            
            if (bluetoothScanCheck != PackageManager.PERMISSION_GRANTED || locationCheck != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Bluetooth and location permissions are required", Toast.LENGTH_LONG).show()
                bind?.progressBar?.visibility = View.INVISIBLE
                return
            }

            val discoveryStarted = mBluetoothAdapter!!.startDiscovery()
            
            if (discoveryStarted) {
                isDiscovering = true
                discoveryTimeoutHandler = Handler(Looper.getMainLooper())
                discoveryTimeoutRunnable = Runnable {
                    if (isDiscovering) {
                        stopBluetoothDiscovery()
                    }
                }
                discoveryTimeoutRunnable?.let {
                    discoveryTimeoutHandler?.postDelayed(it, 10000) 
                }
            } else {
                bind?.progressBar?.visibility = View.INVISIBLE
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Bluetooth permissions are required for device discovery", Toast.LENGTH_LONG).show()
            bind?.progressBar?.visibility = View.INVISIBLE
        } catch (e: InterruptedException) {
            e.printStackTrace()
            bind?.progressBar?.visibility = View.INVISIBLE
        }
    }

    private fun stopBluetoothDiscovery() {
        try {
            if (mBluetoothAdapter?.isDiscovering == true) {
                mBluetoothAdapter?.cancelDiscovery()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        isDiscovering = false
        discoveryTimeoutRunnable?.let { discoveryTimeoutHandler?.removeCallbacks(it) }
        bind?.progressBar?.visibility = View.INVISIBLE
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
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (checkPiAddress(device!!.address)) {
                        addToDialog(device, raspberryDevicesText, raspberryDevices, true)
                    }
                    addToDialog(device, allDevicesText, allDevices, true)
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    isDiscovering = true
                    bind?.progressBar?.visibility = View.VISIBLE
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
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
                e.printStackTrace()
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
        val device = raspberryDevices[position]
        AlertDialog.Builder(activity).setMessage(R.string.delete_device_message)
                .setPositiveButton("Yes") { _, _ ->
                    try {
                        device::class.java.getMethod("removeBond").invoke(device)
                        raspberryDevices.removeAt(position)
                        raspberryDevicesText.removeAt(position)
                        allDevices.remove(device)
                        mArrayAdapter?.notifyDataSetChanged()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(activity, "Unable to delete device", Toast.LENGTH_LONG).show()
                    }
                }.setNegativeButton("No", null).show()

    }
}
