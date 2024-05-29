package io.treehouses.remote.fragments.dialogfragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import io.treehouses.remote.R
import io.treehouses.remote.utils.DialogUtils
import java.util.*


class WifiDialogFragment : DialogFragment() {
    private var mDialog: AlertDialog? = null
    private var wifiManager: WifiManager? = null
    private val wifiList = ArrayList<String>()
    private var mContext: Context? = null
    private var SSID: String? = null
    private var mView: View? = null
    private var firstScan = true
    private var progressBar: ProgressBar? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        val inflater = requireActivity().layoutInflater
        mView = inflater.inflate(R.layout.dialog_listview, null)
        progressBar = mView!!.findViewById(R.id.progressBar)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mContext = context
        setupWifi()
        mDialog = DialogUtils.createAlertDialog(context, mView, R.drawable.dialog_icon)
                .setTitle("Choose a network: ")
                .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
        mDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        return mDialog!!
    }

    private fun setAdapter() {
        val listView = mView!!.findViewById<ListView>(R.id.listView)
        val arrayAdapter = ArrayAdapter(mContext!!, R.layout.simple_list_item, wifiList)
        listView.adapter = arrayAdapter
        listView.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            SSID = wifiList[position]
            if (targetFragment != null) {
                val intent = Intent()
                intent.putExtra(WIFI_SSID_KEY, SSID!!.trim { it <= ' ' })
                targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
                wifiList.clear()
                dismiss()
            }
        }
    }

    private fun setupWifi() {
        wifiManager = mContext!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager == null) return
        wifiManager!!.isWifiEnabled = true
        val wifiScanReceiver = wifiBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        mContext!!.registerReceiver(wifiScanReceiver, intentFilter)
        val success = wifiManager!!.startScan()
        if (!success) {
            scanFailure()
        }
    }

    private fun wifiBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            override fun onReceive(c: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                } else if (firstScan) {
                    scanFailure()
                }
            }
        }
    }

    private fun getSSIDs(results: List<ScanResult>) {
        wifiList.clear()
        // converts Object list to array
        val `object`: Array<Any> = results.toTypedArray()
        val temp = `object`.contentToString()
        val resultArray = temp.split(",".toRegex()).toTypedArray()

        // extracts SSID from wifi data
        for (s in resultArray) {
            if (s.contains("SSID") && !s.contains("BSSID")) {
                val ssid = s.substring(6)

                // add to list if SSID is not hidden
                addToList(ssid)
            }
        }
    }

    private fun addToList(ssid: String) {
        if (ssid.trim { it <= ' ' }.isNotEmpty()) {
            wifiList.add(ssid)
            progressBar!!.visibility = View.INVISIBLE
        }
    }

    private fun scanSuccess() {
        val results = wifiManager!!.scanResults
        getSSIDs(results)
        setAdapter()
    }

    private fun scanFailure() {
        // handle failure: new scan did not succeed
        val results = wifiManager!!.scanResults
        getSSIDs(results)
        if (results.size >= 1 && firstScan) {
            Toast.makeText(context, "Scan unsuccessful. These are old results", Toast.LENGTH_LONG).show()
            setAdapter()
        } else if (results.size < 1 && firstScan) {
            ifResultListEmpty()
        }
        firstScan = false
    }

    private fun ifResultListEmpty() {
        Toast.makeText(context, "Scan unsuccessful, please try again.", Toast.LENGTH_LONG).show()
        dismiss()
    }

    companion object {
        var WIFI_SSID_KEY = "SSID"
        fun newInstance(): DialogFragment {
            return WifiDialogFragment()
        }
    }
}