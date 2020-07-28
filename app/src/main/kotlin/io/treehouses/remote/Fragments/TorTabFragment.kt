package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityTorFragmentBinding
import java.util.*

class TorTabFragment : BaseFragment() {

    override lateinit var mChatService: BluetoothChatService
    private var nowButton: Button? = null
    private var startButton: Button? = null
    private var addPortButton: Button? = null
    private var portsName: ArrayList<String>? = null
    private var adapter: ArrayAdapter<String>? = null
    private var hostName:String = ""
    private var myClipboard: ClipboardManager? = null
    private var myClip: ClipData? = null
    private var portList: ListView? = null
    private var notification: Switch? = null
    var bind: ActivityTorFragmentBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mChatService = listener.getChatService()
        mChatService!!.updateHandler(mHandler)



        listener.sendMessage("treehouses tor ports")


        portsName = ArrayList()
        adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, portsName!!)
        bind = ActivityTorFragmentBinding.inflate(inflater, container, false)
        bind!!.btnHostName.visibility = View.GONE
        bind!!.notifyNow.visibility = View.GONE
        bind!!.switchNotification.visibility = View.GONE
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
        notification = bind!!.switchNotification
        notification!!.isEnabled = false
        notification!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                notification!!.isEnabled = false
                listener.sendMessage("treehouses tor notice on")
            } else {
                notification!!.isEnabled = false
                listener.sendMessage("treehouses tor notice off")
            }
        }
        nowButton = bind!!.notifyNow
        nowButton!!.setOnClickListener {
            nowButton!!.isEnabled = false
            listener.sendMessage("treehouses tor notice now")
        }
        portList = bind!!.portList
        portList!!.adapter = adapter
        portList!!.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
            builder.setTitle("Delete Port " + portsName!![position] + " ?")
//            builder.setMessage("Would you like to delete?");

            // add the buttons
            builder.setPositiveButton("Confirm") { dialog, _ ->
                    listener.sendMessage("treehouses tor delete " + portsName!![position].split(":".toRegex(), 2).toTypedArray()[0])
                    addPortButton!!.text = "deleting port ....."
                    portList!!.isEnabled = false
                    addPortButton!!.isEnabled = false
                    dialog.dismiss()
            }
            builder.setNegativeButton("Cancel", null)

            // create and show the alert dialog
            val dialog = builder.create()
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
        bind!!.btnAddPort
        startButton = bind!!.btnTorStart
        addPortButton = bind!!.btnAddPort
        startButton!!.isEnabled = false
        startButton!!.text = "Getting Tor Status from raspberry pi"
        /* start/stop tor button click */
        startButton!!.setOnClickListener {
            if (startButton!!.text.toString() === "Stop Tor") {
                startButton!!.text = "Stopping Tor"
                startButton!!.isEnabled = false
                listener.sendMessage("treehouses tor stop")
            } else {
                listener.sendMessage("treehouses tor start")
                startButton!!.isEnabled = false
                startButton!!.text = "Starting tor......"
            }
        }
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_tor_ports)
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val window = dialog.window
        window!!.setGravity(Gravity.CENTER)
        window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val inputExternal: TextInputEditText = dialog.findViewById(R.id.ExternalTextInput)
        val inputInternal: TextInputEditText = dialog.findViewById(R.id.InternalTextInput)
        val addingPortButton = dialog.findViewById<Button>(R.id.btn_adding_port)

        addPortButton!!.setOnClickListener {
            inputExternal.clearFocus()
            inputInternal.clearFocus()
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show() }
        addingPortButton.setOnClickListener {
            dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            if (inputExternal.text.toString() !== "" && inputInternal.text.toString() !== "") {
                val s1 = inputInternal.text.toString()
                val s2 = inputExternal.text.toString()
                listener.sendMessage("treehouses tor add $s2 $s1")
                addPortButton!!.text = "Adding port, please wait for a while ............"
                portList!!.isEnabled = false
                addPortButton!!.isEnabled = false
                dialog.dismiss()
                inputInternal.text?.clear()
                inputExternal.text?.clear()
                dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            }
        }
        return bind!!.root
    }
    override fun setUserVisibleHint(visible: Boolean) {
        if(visible) {
            if(isListenerInitialized()){
                mChatService = listener.getChatService()
                mChatService!!.updateHandler(mHandler)




                listener.sendMessage("treehouses tor ports")

                portsName = ArrayList()



            }

        }
    }

    override fun getMessage(msg: Message) {
        if (msg.what == Constants.MESSAGE_READ) {
            val readMessage:String = msg.obj as String
            Log.d("Tor reply", "" + readMessage)
            if (readMessage.contains("inactive")) {
                bind!!.btnHostName.visibility = View.GONE
                bind!!.notifyNow.visibility = View.GONE
                bind!!.switchNotification.visibility = View.GONE
                startButton!!.text = "Start Tor"
                startButton!!.isEnabled = true
                listener.sendMessage("treehouses tor notice")
            } else if (readMessage.contains("the tor service has been stopped") || readMessage.contains("the tor service has been started")) {
                listener.sendMessage("treehouses tor status")
            } else if (readMessage.contains(".onion")) {
                bind!!.btnHostName.visibility = View.VISIBLE
                bind!!.notifyNow.visibility = View.VISIBLE
                bind!!.switchNotification.visibility = View.VISIBLE
                hostName = readMessage
                listener.sendMessage("treehouses tor notice")
            } else if (readMessage.contains("Error")) {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                addPortButton!!.text = "add ports"
                addPortButton!!.isEnabled = true
                portList!!.isEnabled = true
            } else if (readMessage.contains("active")) {
                startButton!!.text = "Stop Tor"
                listener.sendMessage("treehouses tor")
                startButton!!.isEnabled = true
            } else if (readMessage.contains("OK.")) {
                listener.sendMessage("treehouses tor notice")
            } else if (readMessage.contains("Status: on")) {
                notification!!.isChecked = true
                notification!!.isEnabled = true
            } else if (readMessage.contains("Status: off")) {
                notification!!.isChecked = false
                notification!!.isEnabled = true
            } //regex to match ports text
            else if (readMessage.matches("(([0-9]+:[0-9]+)\\s?)+".toRegex())) {
                addPortButton!!.text = "Add Port"
                portList!!.isEnabled = true
                addPortButton!!.isEnabled = true
                val ports = readMessage.split(" ".toRegex()).toTypedArray()
                for (i in ports.indices) {
                    if(i == ports.size - 1){
                        break
                    }
                    portsName!!.add(ports[i])
                }
                adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item, portsName!!)
                val portList = requireView().findViewById<ListView>(R.id.portList)
                portList.adapter = adapter
                listener.sendMessage("treehouses tor status")
            } else if (readMessage.contains("No ports found")) {
                addPortButton!!.text = "Add Port"
                portList!!.isEnabled = true
                addPortButton!!.isEnabled = true
                portsName = ArrayList()
                adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, portsName!!)
                val portList = requireView().findViewById<ListView>(R.id.portList)
                portList.adapter = adapter
                listener.sendMessage("treehouses tor status")
            } else if (readMessage.contains("the port has been added") || readMessage.contains("has been deleted")) {
                listener.sendMessage("treehouses tor ports")
                portsName = ArrayList()
                addPortButton!!.text = "Retrieving port.... Please wait"
                if (readMessage.contains("the port has been added")) {
                    Toast.makeText(requireContext(), "Port added. Retrieving ports list again", Toast.LENGTH_SHORT).show()
                } else if (readMessage.contains("has been deleted")) {
                    Toast.makeText(requireContext(), "Port deleted. Retrieving ports list again", Toast.LENGTH_SHORT).show()
                }
                else if( readMessage.contains("Thanks for the feedback!")){
                    Toast.makeText(requireContext(), "Notified Gitter. Thank you!", Toast.LENGTH_SHORT).show()
                    nowButton!!.isEnabled = true
                }
            }
        }
    }
}