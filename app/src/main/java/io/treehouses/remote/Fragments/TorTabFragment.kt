package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityTorFragmentBinding
import java.util.*

class TorTabFragment : BaseFragment() {

    override lateinit var mChatService: BluetoothChatService
    private var startButton: Button? = null
    private val moreButton: Button? = null
    private var addPortButton: Button? = null
    private var textStatus: TextView? = null
    private var portsName: ArrayList<String>? = null
    private var adapter: ArrayAdapter<String>? = null

    private val background: ImageView? = null
    private var logo: ImageView? = null
    private val internetstatus: ImageView? = null
    private var myClipboard: ClipboardManager? = null
    private var myClip: ClipData? = null
    private val nDialog: ProgressDialog? = null
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
        notification = bind!!.switchNotification
        notification!!.isEnabled = false
        notification!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                notification!!.isEnabled = false
                listener.sendMessage("treehouses tor notice on")
            } else {
                notification!!.isEnabled = false
                listener.sendMessage("treehouses tor notice off")
            }
        }
        portList = bind!!.countries
        portList!!.setAdapter(adapter)
        portList!!.setOnItemClickListener(OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete Port " + portsName!![position] + " ?")

//            builder.setMessage("Would you like to delete?");

            // add the buttons
            builder.setPositiveButton("Confirm") { dialog, id ->
                listener.sendMessage("treehouses tor delete " + portsName!![position].split(":".toRegex(), 2).toTypedArray()[0])
                addPortButton!!.text = "deleting port ....."
                portList!!.setEnabled(false)
                addPortButton!!.isEnabled = false
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel", null)

            // create and show the alert dialog
            val dialog = builder.create()
            dialog.show()
        })
        logo = bind!!.treehouseLogo
        bind!!.btnAddPort
        startButton = bind!!.btnTorStart
        addPortButton = bind!!.btnAddPort
        startButton!!.setEnabled(false)
        startButton!!.setText("Getting Tor Status from raspberry pi")
        textStatus = bind!!.torStatusText
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        textStatus!!.setText("-")
        val filter = ColorMatrixColorFilter(matrix)
        logo!!.setColorFilter(filter)
        /* start/stop tor button click */startButton!!.setOnClickListener(View.OnClickListener { v: View? ->
            if (startButton!!.getText().toString() === "Stop Tor") {
                startButton!!.setText("Stopping Tor")
                startButton!!.setEnabled(false)
                listener.sendMessage("treehouses tor stop")
            } else {
                listener.sendMessage("treehouses tor start")
                startButton!!.setEnabled(false)
                startButton!!.setText("Starting tor......")
            }
        })
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_tor_ports)
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val window = dialog.window
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val inputExternal: TextInputEditText = dialog.findViewById(R.id.ExternalTextInput)
        val inputInternal: TextInputEditText = dialog.findViewById(R.id.InternalTextInput)
        val addingPortButton = dialog.findViewById<Button>(R.id.btn_adding_port)

        addPortButton!!.setOnClickListener(View.OnClickListener { v: View? -> dialog.show() })
        addingPortButton.setOnClickListener { v: View? ->
            if (inputExternal.text.toString() !== "" && inputInternal.text.toString() !== "") {
                val s1 = inputInternal.text.toString()
                val s2 = inputExternal.text.toString()
                listener.sendMessage("treehouses tor add $s2 $s1")
                addPortButton!!.setText("Adding port, please wait for a while ............")
                portList!!.setEnabled(false)
                addPortButton!!.setEnabled(false)
                dialog.dismiss()
                dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            }
        }
        textStatus!!.setOnClickListener(View.OnClickListener { v: View? ->
            myClipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text: String
            text = textStatus!!.getText().toString()
            myClip = ClipData.newPlainText("text", textStatus!!.getText())
            myClipboard!!.setPrimaryClip(myClip!!)
            Toast.makeText(requireContext(), textStatus!!.getText().toString() + " copied!", Toast.LENGTH_SHORT).show()
        })
        /* more button click */
//            moreButton.setOnClickListener(v ->{
//                showBottomSheet(new TorBottomSheet(), "ethernet");
//            });
        return bind!!.root
    }

    private fun showBottomSheet(fragment: BottomSheetDialogFragment, tag: String) {
        fragment.setTargetFragment(this@TorTabFragment, Constants.NETWORK_BOTTOM_SHEET)
        fragment.show(requireFragmentManager(), tag)
    }

    override fun getMessage(msg: Message) {
        if (msg.what == Constants.MESSAGE_READ) {
            val readMessage:String = msg.obj as String
            Log.d("Tor reply", "" + readMessage)
            if (readMessage.contains("inactive")) {
                val matrix = ColorMatrix()
                matrix.setSaturation(0f)
                textStatus!!.text = "-"
                val filter = ColorMatrixColorFilter(matrix)
                logo!!.colorFilter = filter
                startButton!!.text = "Start Tor"
                textStatus!!.text = "-"
                startButton!!.isEnabled = true
                listener.sendMessage("treehouses tor notice")
            } else if (readMessage.contains("the tor service has been stopped") || readMessage.contains("the tor service has been started")) {
                listener.sendMessage("treehouses tor status")
            } else if (readMessage.contains(".onion")) {
                textStatus!!.text = readMessage
                listener.sendMessage("treehouses tor notice")
            } else if (readMessage.contains("Error")) {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                addPortButton!!.text = "add ports"
                addPortButton!!.isEnabled = true
                portList!!.isEnabled = true
            } else if (readMessage.contains("active")) {
                val matrix = ColorMatrix()
                val filter = ColorMatrixColorFilter(matrix)
                logo!!.colorFilter = filter
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
                    portsName!!.add(ports[i])
                }
                adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, portsName!!)
                val portList = view!!.findViewById<ListView>(R.id.countries)
                portList.adapter = adapter
                listener.sendMessage("treehouses tor status")
            }else if (readMessage.contains("the port has been added") || readMessage.contains("has been deleted")) {
                listener.sendMessage("treehouses tor ports")
                portsName = ArrayList()
                addPortButton!!.text = "Retrieving port.... Please wait"
                if (readMessage.contains("the port has been added")) {
                    Toast.makeText(requireContext(), "Port added. Retrieving ports list again", Toast.LENGTH_SHORT).show()
                } else if (readMessage.contains("has been deleted")) {
                    Toast.makeText(requireContext(), "Port deleted. Retrieving ports list again", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}