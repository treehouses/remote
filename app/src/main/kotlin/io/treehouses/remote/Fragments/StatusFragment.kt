package io.treehouses.remote.Fragments

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.bases.BaseStatusFragment
import io.treehouses.remote.callback.NotificationCallback
import io.treehouses.remote.databinding.ActivityStatusFragmentBinding
import io.treehouses.remote.databinding.DialogRenameStatusBinding
import io.treehouses.remote.pojo.StatusData
import kotlinx.android.synthetic.main.dialog_wificountry.*
import java.util.*
import io.treehouses.remote.utils.logD
import io.treehouses.remote.utils.logE
import kotlinx.android.synthetic.main.activity_status_fragment.*

class StatusFragment : BaseStatusFragment() {

    private var lastCommand = ""
    private var deviceName = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityStatusFragmentBinding.inflate(inflater, container, false)
        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)
        deviceName = mChatService.connectedDeviceName

        checkStatusNow()
        val countriesCode = Locale.getISOCountries()
        val countriesName = arrayOfNulls<String>(countriesCode.size)
        for (i in countriesCode.indices) {
            countriesName[i] = getCountryName(countriesCode[i])
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item_countries, countriesName)
        bind.countryDisplay.isEnabled = false
        bind.countryDisplay.setOnClickListener{ wifiCountry(adapter) }
        refresh()
        bind.refreshBtn.setOnClickListener { refresh() }

        return bind.root
    }

    private fun wifiCountry(adapter:ArrayAdapter<String?>){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_wificountry)
        dialog.countries
        countryList = dialog.countries
        adapter.filter.filter("")
        countryList!!.adapter = adapter
        countryList!!.isTextFilterEnabled = true
        countryList!!.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, p: Int, _: Long ->
            var selectedString = countryList!!.getItemAtPosition(p).toString()
            selectedString = selectedString.substring(selectedString.length - 4, selectedString.length - 2)
            writeToRPI(requireContext().resources.getString(R.string.TREEHOUSES_WIFI_COUNTRY, selectedString))
            bind.countryDisplay.isEnabled = false
            bind.countryDisplay.setText("Changing country")
            dialog.dismiss()
        }

        searchView(dialog)
        dialog.show()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addRefreshListener(view)
        bind.tvBluetooth.text = deviceName
        logD("STATUS device name: $deviceName")
        upgradeOnViewClickListener()
        rpiNameOnViewClickListener()
        Tutorials.statusTutorials(bind, requireActivity())
        bind.upgrade.visibility = View.GONE
    }

    private fun addRefreshListener(view: View) {
        bind.swiperefresh.setOnRefreshListener {
            refresh()
        }
        bind.swiperefresh.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_light),
                ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light),
                ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light),
                ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
    }

    private fun upgradeOnViewClickListener() {
        bind.upgrade.setOnClickListener {
            writeToRPI(requireActivity().getString(R.string.TREEHOUSES_UPGRADE))
            updateRightNow = true
            bind.progressBar.visibility = View.VISIBLE
            bind.upgrade.visibility = View.GONE
        }
    }

    private fun rpiNameOnViewClickListener() {
        bind.editName.setOnClickListener {showRenameDialog()}
    }

    override fun updateStatus(readMessage: String) {
        logD("$TAG updateStatus: $lastCommand response $readMessage")

        if(lastCommand == requireActivity().getString(R.string.TREEHOUSES_REMOTE_STATUSPAGE)){
            val statusData = Gson().fromJson(readMessage, StatusData::class.java)

            bind.temperature.text = statusData.temperature + "°C"
            ObjectAnimator.ofInt(bind.temperatureBar, "progress", (statusData.temperature.toFloat() / 80 * 100).toInt()).setDuration(600).start()

            val usedMemory = statusData.memory_used.trim { it <= ' ' }.toDouble()
            val totalMemory = statusData.memory_total.trim { it <= ' ' }.toDouble()

            val usedStoragePercentage = statusData.storage.split(" ")[3].dropLast(1)
            ObjectAnimator.ofInt(bind.storageBar, "progress", usedStoragePercentage.toInt()).setDuration(600).start()
            bind.storage.text = statusData.storage.split(" ")[2].dropLast(1).replace("G", "GB")

            ObjectAnimator.ofInt(bind.memoryBar, "progress", (usedMemory/totalMemory*100).toInt()).setDuration(600).start()
            bind.memory.text = usedMemory.toString() + "GB" + "/" + totalMemory.toString() + "GB"

            bind.cpuModelText.text = "CPU: ARM " + statusData.arm

            writeNetworkInfo(statusData.networkmode, statusData.info)

            bind.tvRpiName.text = "Hostname: " + statusData.hostname

            updateStatusPage(statusData)

        } else checkUpgradeStatus(readMessage)
    }

    override fun checkWifiStatus(readMessage: String) {
        if (readMessage.startsWith("true")) {
            writeToRPI(requireActivity().getString(R.string.TREEHOUSES_UPGRADE_CHECK))
        } else {
            bind.tvUpgradeCheck.text = "      NO INTERNET"
            bind.upgrade.visibility = View.GONE
        }
    }

    override fun writeToRPI(ping: String) {
        lastCommand = ping
        val pSend = ping.toByteArray()
        mChatService.write(pSend)
    }

    private fun showRenameDialog() {
        val inflater = requireActivity().layoutInflater
        val dialogBinding = DialogRenameStatusBinding.inflate(inflater)
        dialogBinding.hostname.hint = "New Name"
        val alertDialog = createRenameDialog(dialogBinding.root, dialogBinding.hostname)
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    private fun createRenameDialog(view: View, mEditText: EditText): AlertDialog {
        return AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle))
                .setView(view).setTitle("Rename " + deviceName.substring(0, deviceName.indexOf("-"))).setIcon(R.drawable.dialog_icon)
                .setPositiveButton("Rename"
                ) { _: DialogInterface?, _: Int ->
                    if (mEditText.text.toString() != "") {
                        writeToRPI(requireActivity().getString(R.string.TREEHOUSES_RENAME, mEditText.text.toString()))
                        Toast.makeText(context, "Raspberry Pi Renamed", Toast.LENGTH_LONG).show()
                        refresh()
                    } else {
                        Toast.makeText(context, "Please enter a new name", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        notificationListener = try {
            getContext() as NotificationCallback?
        } catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement NotificationListener")
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    override fun getMessage(msg: Message) {
        when (msg.what) {
            Constants.MESSAGE_STATE_CHANGE -> checkStatusNow()
            Constants.MESSAGE_WRITE -> {
                val writeBuf = msg.obj as ByteArray
                val writeMessage = String(writeBuf)
                logE("$TAG, writeMessage = $writeMessage")
            }
            Constants.MESSAGE_READ -> {
                val readMessage = msg.obj as String
                logE("$TAG, readMessage = $readMessage")
                receiveMessage(readMessage)
            }
        }
    }

    companion object {
        private const val TAG = "StatusFragment"
    }
}