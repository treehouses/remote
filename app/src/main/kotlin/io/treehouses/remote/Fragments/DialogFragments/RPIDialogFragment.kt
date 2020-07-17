package io.treehouses.remote.Fragments.DialogFragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.adapter.RPIListAdapter
import io.treehouses.remote.bases.BaseDialogFragment
import io.treehouses.remote.callback.SetDisconnect
import io.treehouses.remote.databinding.ActivityRpiDialogFragmentBinding
import io.treehouses.remote.pojo.DeviceInfo
import java.util.*

class RPIDialogFragment : BaseDialogFragment() {
    private val raspberryDevices: MutableList<BluetoothDevice> = ArrayList()
    private val allDevices: MutableList<BluetoothDevice> = ArrayList()
    private var pairedDevices: Set<BluetoothDevice>? = null
    private var mArrayAdapter: ArrayAdapter<*>? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var checkConnectionState: SetDisconnect? = null
    private var mContext:Context? = null
    private var mDialog: AlertDialog? = null
    private val raspberryDevicesText: MutableList<DeviceInfo> = ArrayList()
    private val allDevicesText: MutableList<DeviceInfo> = ArrayList()
    private var pDialog: ProgressDialog? = null
    private var bind: ActivityRpiDialogFragmentBinding? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mContext = requireContext()
        instance = this
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothCheck()
        if (mBluetoothAdapter!!.isDiscovering) {
            mBluetoothAdapter!!.cancelDiscovery()
        }
        mBluetoothAdapter!!.startDiscovery()
        bind = ActivityRpiDialogFragmentBinding.inflate(requireActivity().layoutInflater)
        initDialog()
        if (mChatService == null) {
            mChatService = BluetoothChatService(mHandler, requireActivity().applicationContext)
        }
        pairedDevices = mBluetoothAdapter!!.bondedDevices
        setAdapterNotNull(raspberryDevicesText)
        for (d in pairedDevices!!) {
            if (checkPiAddress(d.address)) {
                addToDialog(d, raspberryDevicesText, raspberryDevices, false)
                bind!!.progressBar.visibility = View.INVISIBLE
            }
        }
        intentFilter()
        mDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        return mDialog!!
    }

    private fun initDialog() {
        pDialog = ProgressDialog(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
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
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        requireActivity().registerReceiver(mReceiver, filter)
    }

    private fun listViewOnClickListener(mView: View) {
        bind!!.listView.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            mChatService = BluetoothChatService(mHandler, requireActivity().applicationContext)
            val deviceList: List<BluetoothDevice> = if (bind!!.rpiSwitch.isChecked) raspberryDevices else allDevices
            if (checkPiAddress(deviceList[position].address)) {
                mainDevice = deviceList[position]
                mChatService!!.connect(deviceList[position], true)
                val status = mChatService!!.state
                mDialog!!.cancel()
                finish(status, mView)
                Log.e("Connecting Bluetooth", "Position: $position ;; Status: $status")
                pDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                pDialog!!.setTitle("Connecting...")
                pDialog!!.setMessage("""
    Device Name: ${mainDevice!!.name}
    Device Address: ${mainDevice!!.address}
    """.trimIndent())
                pDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                pDialog!!.show()
            } else {
                Toast.makeText(context, "Device Unsupported", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun switchViewOnClickListener() {
        bind!!.rpiSwitch.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                setAdapterNotNull(raspberryDevicesText)
                buttonView.setText(R.string.paired_devices)
                if (raspberryDevices.isEmpty()) bind!!.progressBar.visibility = View.VISIBLE
            } else {
                setAdapterNotNull(allDevicesText)
                buttonView.setText(R.string.all_devices)
                bind!!.progressBar.visibility = View.INVISIBLE
            }
        }
    }

    fun setCheckConnectionState(checkConnectionState: SetDisconnect?) {
        this.checkConnectionState = checkConnectionState
    }

    private fun finish(status: Int, mView: View) {
        val mDialog = getAlertDialog(mView)
        mDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        if (status == 3) mDialog.setTitle("BLUETOOTH IS CONNECTED") else if (status == 2) mDialog.setTitle("BLUETOOTH IS CONNECTING...") else mDialog.setTitle("BLUETOOTH IS NOT CONNECTED")
        setAdapterNotNull(ArrayList())
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            requireContext().unregisterReceiver(mReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAlertDialog(mView: View): AlertDialog {
        return AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle)).setView(mView).setIcon(R.drawable.dialog_icon).create()
    }

    fun bluetoothCheck(vararg args: String) {
        if (mBluetoothAdapter == null) {
            Toast.makeText(activity, "Your Bluetooth Is Not Enabled or Not Supported", Toast.LENGTH_LONG).show()
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, requireActivity().intent)
            try {
                mContext!!.unregisterReceiver(mReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (args.isNotEmpty() && args[0] == "unregister") {
            try {
                mContext!!.unregisterReceiver(mReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val intent = Intent()
            intent.putExtra("mChatService", mChatService)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        }
    }

    private fun setAdapterNotNull(listVal: List<DeviceInfo>) {
        mArrayAdapter = RPIListAdapter(requireContext(), listVal)
        bind!!.listView.adapter = mArrayAdapter
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (checkPiAddress(device!!.address)) {
                    addToDialog(device, raspberryDevicesText, raspberryDevices, true)
                    bind!!.progressBar.visibility = View.INVISIBLE
                }
                addToDialog(device, allDevicesText, allDevices, true)
                Log.e("Broadcast BT", """
     ${device.name}
     ${device.address}
     """.trimIndent())
            }
        }
    }

    private fun addToDialog(device: BluetoothDevice, textList: MutableList<DeviceInfo>, mDevices: MutableList<BluetoothDevice>, inRange: Boolean) {
        if (!mDevices.contains(device)) {
            mDevices.add(device)
            textList.add(DeviceInfo("""
    ${device.name}
    ${device.address}
    """.trimIndent(), pairedDevices!!.contains(device), inRange))
        } else textList[mDevices.indexOf(device)].isInRange = true
        mArrayAdapter!!.notifyDataSetChanged()
    }

    private fun checkPiAddress(deviceHardwareAddress: String): Boolean {
        val piAddress: Set<String> = HashSet(listOf("B8:27:EB", "DC:A6:32", "B8-27-EB", "DC-A6-32", "B827.EB", "DCA6.32"))
        return piAddress.contains(deviceHardwareAddress.substring(0, 7)) || piAddress.contains(deviceHardwareAddress.substring(0, 8))
    }

    @SuppressLint("HandlerLeak")
    val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            Log.e("RPIDialogFragment", "" + msg.what)
            val readMessage = msg.obj as? String
            if (!TextUtils.isEmpty(readMessage) && readMessage == "connectionCheck") pDialog!!.dismiss()
            when (msg.what) {
                Constants.MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                    Constants.STATE_CONNECTED -> {
                        Log.e("RPIDialogFragment", "Bluetooth Connection Status Change: State Listen")
                        pDialog!!.dismiss()
                        listener!!.setChatService(mChatService!!)
                        checkConnectionState!!.checkConnectionState()
                        mBluetoothAdapter!!.cancelDiscovery()
                        Toast.makeText(mContext, "Bluetooth Connected", Toast.LENGTH_LONG).show()
                    }
                    Constants.STATE_NONE -> {
                        pDialog!!.dismiss()
                        Toast.makeText(mContext, "Connection Failed: Please Try Again", Toast.LENGTH_LONG).show()
                        Log.e("RPIDialogFragment", "Bluetooth Connection Status Change: State None")
                    }
                }
                Constants.MESSAGE_DEVICE_NAME -> Log.e("RPIDialogFragment", "Device Name " + msg.data.getString(Constants.DEVICE_NAME))
            }
        }
    }

    companion object {
        private var mChatService: BluetoothChatService? = null
        var instance: RPIDialogFragment? = null
            private set
        private var mainDevice: BluetoothDevice? = null
        fun newInstance(num: Int): DialogFragment {
            val rpiDialogFragment = RPIDialogFragment()
            val bundle = Bundle()
            bundle.putInt("num", num)
            rpiDialogFragment.arguments = bundle
            return rpiDialogFragment
        }

    }
}