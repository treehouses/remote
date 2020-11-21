package io.treehouses.remote.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Message
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.adapter.TunnelPortAdapter
import io.treehouses.remote.bases.BaseTorTabFragment
import io.treehouses.remote.databinding.ActivityTorFragmentBinding
import io.treehouses.remote.utils.Utils
import io.treehouses.remote.utils.logE
import java.util.*

class TorTabFragment : BaseTorTabFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mChatService = listener.getChatService()
        mChatService!!.updateHandler(mHandler)
        listener.sendMessage(getString(R.string.TREEHOUSES_TOR_PORTS))
        portsName = ArrayList()
        adapter = TunnelPortAdapter(requireContext(), portsName!!)
        bind = ActivityTorFragmentBinding.inflate(inflater, container, false)
        bind!!.btnHostName.visibility = View.GONE
        addHostNameButonListener()
        notification = bind!!.switchNotification
        notification!!.isEnabled = false
        addNotificationListener()
        nowButton = bind!!.notifyNow
        addNowButtonListener()
        portList = bind!!.portList
        portList!!.adapter = adapter
        addPortListListener()
        initializeProperties()
        /* start/stop tor button click */
        addStartButtonListener()
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_tor_ports)
        setWindowProperties(dialog)
        addPortButtonListeners(dialog)
        return bind!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind?.let { Tutorials.tunnelTorTutorials(it, requireActivity()) }
    }
    private fun addHostNameButonListener() {
        bind!!.btnHostName.setOnClickListener {
            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
            builder.setTitle("Tor Hostname")
            builder.setMessage(hostName)
            builder.setPositiveButton("Copy") { _, _ ->
                myClipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                myClip = ClipData.newPlainText("text", hostName)
                myClipboard!!.setPrimaryClip(myClip!!)
                Toast.makeText(requireContext(), "$hostName copied!", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("Exit", null)
            val dialog = builder.create()
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
    }

    private fun addNowButtonListener() {
        val messages = Pair(getString(R.string.TREEHOUSES_TOR_NOTICE_NOW), "The Gitter Channel has been notified.")
        nowButton!!.setOnClickListener {
            Utils.sendMessage(listener, messages, requireContext(), Toast.LENGTH_SHORT)
        }
    }

    private fun addNotificationListener() {
        val noticeOn = getString(R.string.TREEHOUSES_TOR_NOTICE_ON)
        val noticeOff = getString(R.string.TREEHOUSES_TOR_NOTICE_OFF)
        notification!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) listener.sendMessage(noticeOn)
            else listener.sendMessage(noticeOff)
            notification!!.isEnabled = false
        }
    }

    private fun addPortListListener() {
        portList!!.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val deleteAllPortsButtonSelected = portsName!!.size > 1 && position == portsName!!.size-1
            if (deleteAllPortsButtonSelected) promptDeleteAllPorts()
            else promptDeletePort(position)
        }

        bind!!.btnAddPort
        startButton = bind!!.btnTorStart
        addPortButton = bind!!.btnAddPort
        startButton!!.isEnabled = false
        startButton!!.text = "Getting Tor Status from raspberry pi"
    }


    private fun addPortButtonListeners(dialog: Dialog) {
        val inputExternal: TextInputEditText = dialog.findViewById(R.id.ExternalTextInput)
        val inputInternal: TextInputEditText = dialog.findViewById(R.id.InternalTextInput)
        addPortButton!!.setOnClickListener {
            inputExternal.clearFocus()
            inputInternal.clearFocus()
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
        val addingPortButton = dialog.findViewById<Button>(R.id.btn_adding_port)
        addingPortButton.setOnClickListener {
            dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            if (inputExternal.text.toString() !== "" && inputInternal.text.toString() !== "") {
                val s1 = inputInternal.text.toString();
                val s2 = inputExternal.text.toString()
                listener.sendMessage(getString(R.string.TREEHOUSES_TOR_ADD, s2, s1))
                addPortButton!!.text = "Adding port. Please wait..."
                portList!!.isEnabled = false; addPortButton!!.isEnabled = false
                dialog.dismiss()
                inputInternal.text?.clear(); inputExternal.text?.clear()
                dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            }
        }
        dialog.findViewById<ImageButton>(R.id.closeButton).setOnClickListener { dialog.dismiss() }
    }

    private fun addStartButtonListener() {
        startButton!!.setOnClickListener {
            if (startButton!!.text.toString() === "Stop Tor") {
                startButton!!.text = "Stopping Tor"
                startButton!!.isEnabled = false
                listener.sendMessage(getString(R.string.TREEHOUSES_TOR_STOP))
            } else {
                listener.sendMessage(getString(R.string.TREEHOUSES_TOR_START))
                startButton!!.isEnabled = false
                startButton!!.text = "Starting Tor..."
            }
        }
    }

    override fun getMessage(msg: Message) {
        if (!isAttachedToActivity()) return
        if (msg.what == Constants.MESSAGE_READ) {
            val readMessage: String = msg.obj as String
            if (readMessage.contains("inactive")) {
                bind!!.btnHostName.visibility = View.GONE
                startButton!!.text = "Start Tor"
                startButton!!.isEnabled = true
                listener.sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE))
            } else if (readMessage.contains(".onion")) {
                bind!!.btnHostName.visibility = View.VISIBLE
                hostName = readMessage
                listener.sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE))
            } else if (readMessage.contains("Error")) {
                Toast.makeText(requireContext(), "Error, add a port if its your first time", Toast.LENGTH_SHORT).show()
                addPortButton!!.text = "add ports"
                addPortButton!!.isEnabled = true
                portList!!.isEnabled = true
            } else if (readMessage.contains("active")) {
                startButton!!.text = "Stop Tor"
                listener.sendMessage(getString(R.string.TREEHOUSES_TOR))
                startButton!!.isEnabled = true
            } else handleOtherMessages(readMessage)
        }
    }

    private fun handleOtherMessages(readMessage: String) {

        if (readMessage.contains("OK.")) listener.sendMessage(requireActivity().getString(R.string.TREEHOUSES_TOR_NOTICE))
        else if (readMessage.contains("Status: on")) {
            notification!!.isChecked = true; notification!!.isEnabled = true
        } else if (readMessage.contains("Status: off")) {
            notification!!.isChecked = false; notification!!.isEnabled = true
        } //regex to match ports text
        else if (readMessage.matches("(([0-9]+:[0-9]+)\\s?)+".toRegex())) {
            addPortButton!!.text = "Add Port"
            portList!!.isEnabled = true
            addPortButton!!.isEnabled = true
            val ports = readMessage.split(" ".toRegex()).toTypedArray()
            for (i in ports.indices) {
                if (i == ports.size - 1) break
                portsName!!.add(ports[i])
            }
            if (portsName!!.size > 1) portsName!!.add("All")
            try { adapter = TunnelPortAdapter(requireContext(), portsName!!) } catch (e: Exception) { logE(e.toString()) }
            val portList = requireView().findViewById<ListView>(R.id.portList)
            portList.adapter = adapter
            listener.sendMessage(getString(R.string.TREEHOUSES_TOR_STATUS))
        } else handleMoreMessages(readMessage)
    }

    private fun handleMoreMessages(readMessage: String) {
        if (readMessage.contains("No ports found")) {
            addPortButton!!.text = "Add Port"
            portList!!.isEnabled = true; addPortButton!!.isEnabled = true
            portsName = ArrayList()
            adapter = TunnelPortAdapter(requireContext(), portsName!!)
            val portList = requireView().findViewById<ListView>(R.id.portList)
            portList.adapter = adapter
            listener.sendMessage(getString(R.string.TREEHOUSES_TOR_STATUS))
        } else if (readMessage.contains("the port has been added") || readMessage.contains("has been deleted")) {
            listener.sendMessage(getString(R.string.TREEHOUSES_TOR_PORTS))
            portsName = ArrayList()
            addPortButton!!.text = "Retrieving port. Please wait..."
            if (readMessage.contains("the port has been added")) {
                Toast.makeText(requireContext(), "Port added. Retrieving ports list again", Toast.LENGTH_SHORT).show()
            } else if (readMessage.contains("has been deleted")) {
                Toast.makeText(requireContext(), "Port deleted. Retrieving ports list again", Toast.LENGTH_SHORT).show()
            } else handleFurtherMessages(readMessage)
        } else if (readMessage.contains("ports have been deleted")) {
            listener.sendMessage(getString(R.string.TREEHOUSES_TOR_PORTS))
            portsName = ArrayList()
        }
    }

    private fun handleFurtherMessages(readMessage: String) {
        if (readMessage.contains("Thanks for the feedback!")) {
            Toast.makeText(requireContext(), "Notified Gitter. Thank you!", Toast.LENGTH_SHORT).show()
            nowButton!!.isEnabled = true
        } else if (readMessage.contains("the tor service has been stopped") || readMessage.contains("the tor service has been started")) {
            listener.sendMessage(getString(R.string.TREEHOUSES_TOR_STATUS))
        }
    }
}
