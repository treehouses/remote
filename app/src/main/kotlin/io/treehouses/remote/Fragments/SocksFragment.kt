package io.treehouses.remote.Fragments
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivitySocksFragmentBinding
import io.treehouses.remote.databinding.DialogAddProfileBinding
import io.treehouses.remote.utils.logD

import kotlin.collections.ArrayList

class SocksFragment : BaseFragment() {

    override lateinit var mChatService: BluetoothChatService
    private var startButton: Button? = null
    private var addProfileButton: Button? = null
    private var addingProfileButton: Button? = null
    private var cancelProfileButton: Button? = null
    private var textStatus: TextView? = null
    private var adapter: ArrayAdapter<String>? = null
    private var profileName: java.util.ArrayList<String>? = null
    private var portList: ListView? = null
    private lateinit var dialog:Dialog
    private lateinit var password: EditText
    private lateinit var serverPort: EditText
    private lateinit var localPort: EditText
    private lateinit var localAddress: EditText
    private lateinit var serverHost: EditText
    var bind: ActivitySocksFragmentBinding? = null
    var bindProfile: DialogAddProfileBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivitySocksFragmentBinding.inflate(inflater, container, false)
        bindProfile = DialogAddProfileBinding.inflate(inflater, container, false)
        profileName = ArrayList()
        addProfileButton = bind!!.btnAddProfile
        portList = bind!!.profiles
        initializeDialog()
        addProfileButtonListeners(dialog)
        portList = bind!!.profiles
        return bind!!.root
    }
    private fun initializeDialog(){
        dialog = Dialog(requireContext())
        addPortListListener()
        dialog.setContentView(bindProfile!!.root)
        serverHost = bindProfile!!.ServerHost
        localAddress = bindProfile!!.LocalAddress
        localPort = bindProfile!!.localPort
        serverPort = bindProfile!!.serverPort
        password = bindProfile!!.password
        addingProfileButton = bindProfile!!.addingProfileButton
        cancelProfileButton = bindProfile!!.cancel
        val window = dialog.window
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

    }
    private fun addPortListListener() {
        portList!!.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
            val selectedString = profileName!![position]
            builder.setTitle("Delete Profile $selectedString ?")
            builder.setPositiveButton("Confirm") { dialog, _ ->
                listener.sendMessage("treehouses shadowsocks remove $selectedString ")
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel", null)

            // create and show the alert dialog
            val dialog = builder.create()
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
    }

    private fun addProfileButtonListeners(dialog: Dialog) {

        addProfileButton!!.setOnClickListener {
            dialog.show()
        }
        cancelProfileButton!!.setOnClickListener {
            dialog.dismiss()
        }
        addingProfileButton!!.setOnClickListener {
            if (serverHost.text.toString().isNotEmpty() && localAddress.text.toString().isNotEmpty() && localPort.text.toString().isNotEmpty() && serverPort.text.toString().isNotEmpty() && password.text.toString().isNotEmpty()) {
                val ServerHost = serverHost.text.toString()
                val LocalAddress = localAddress.text.toString()
                val LocalPort = localPort.text.toString()
                val ServerPort = serverPort.text.toString()
                val Password = password.text.toString()

                val message = "treehouses shadowsocks add { \\\"server\\\": \\\"$ServerHost\\\", \\\"local_address\\\": \\\"$LocalAddress\\\", \\\"local_port\\\": $LocalPort, \\\"server_port\\\": $ServerPort, \\\"password\\\": \\\"$Password\\\", \\\"method\\\": \\\"rc4-md5\\\" }"
                listener.sendMessage(message)
                addProfileButton!!.text = "Adding......"
                addProfileButton!!.isEnabled = false
                dialog.dismiss()
            }
            else{
                Toast.makeText(requireContext(), "Missing Information", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun setUserVisibleHint(visible: Boolean) {
        if (visible) {
            if (isListenerInitialized()) {
                mChatService = listener.getChatService()
                mChatService!!.updateHandler(mHandler)
                profileName = ArrayList()
                listener.sendMessage("treehouses shadowsocks list")
            }

        }
    }

    override fun getMessage(msg: Message) {
        logD("SOCKS MESSAGE " + msg)
        if (msg.what == Constants.MESSAGE_READ) {
            val readMessage: String = msg.obj as String
            if (readMessage.contains("inactive")) {
                textStatus!!.text = "-"; startButton!!.text = "Start Tor"
                startButton!!.isEnabled = true
                listener.sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE))
            }
            else if(readMessage.contains("Error when")){
                profileName = ArrayList()
                listener.sendMessage("treehouses shadowsocks list")
            }
            else if(readMessage.contains("Use `treehouses shadowsock")){
                addProfileButton!!.text = "Add Profile"
                addProfileButton!!.isEnabled = true
                profileName = ArrayList()
                listener.sendMessage("treehouses shadowsocks list")
            }
            else{
                getMessage2(readMessage)
            }
        }
    }

    private fun getMessage2(readMessage: String) {
        if(readMessage.contains("removed")){
            Toast.makeText(requireContext(), "Removed, retrieving list again", Toast.LENGTH_SHORT).show()
            profileName = ArrayList()
            listener.sendMessage("treehouses shadowsocks list")
        }
        else if (readMessage.contains("tmptmp") && !readMessage.contains("disabled") && !readMessage.contains("stopped")){

            if(readMessage.contains(' '))
                profileName!!.add(readMessage.split(' ')[0])
            else
                profileName!!.add(readMessage)

            adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item, profileName!!)
            bind!!.profiles.adapter = adapter
        }
    }
}
