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
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivitySocksFragmentBinding
import kotlinx.android.synthetic.main.dialog_add_profile.*
import java.util.*
import kotlin.collections.ArrayList

class SocksFragment : BaseFragment() {

    override lateinit var mChatService: BluetoothChatService
    private var nowButton: Button? = null
    private var startButton: Button? = null
    private var addProfileButton: Button? = null
    private var addingProfileButton: Button? = null
    private var textStatus: TextView? = null
    private var portsName: ArrayList<String>? = null
    private var adapter: ArrayAdapter<String>? = null
    private var profileName: java.util.ArrayList<String>? = null
    private var myClipboard: ClipboardManager? = null
    private var myClip: ClipData? = null
    private var portList: ListView? = null
    private var notification: Switch? = null
    private lateinit var dialog:Dialog
    private lateinit var password: AppCompatEditText
    private lateinit var serverPort: AppCompatEditText
    private lateinit var localPort: AppCompatEditText
    private lateinit var localAddress: AppCompatEditText
    private lateinit var serverHost: AppCompatEditText
    var bind: ActivitySocksFragmentBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mChatService = listener.getChatService()
        bind = ActivitySocksFragmentBinding.inflate(inflater, container, false)
        mChatService!!.updateHandler(mHandler)
        profileName = ArrayList()
        listener.sendMessage("treehouses shadowsocks list")
addProfileButton = bind!!.btnAddProfile
        initializeDialog()
        addProfileButtonListeners(dialog)

        portList = bind!!.profiles
        return bind!!.root
    }
    private fun initializeDialog(){
        dialog = Dialog(requireContext())


        dialog.setContentView(R.layout.dialog_add_profile)

        serverHost = dialog.findViewById(R.id.Server_Host)
        localAddress = dialog.findViewById(R.id.Local_Address)
        localPort = dialog.findViewById(R.id.local_port)
        serverPort = dialog.findViewById(R.id.server_port)
        password = dialog.findViewById(R.id.password)
        addingProfileButton = dialog.findViewById(R.id.addingProfileButton)
//        inputExternal = dialog.findViewById(R.id.ExternalTextInput)
//        inputInternal = dialog.findViewById(R.id.InternalTextInput)
//
//        addingPortButton = dialog.findViewById<Button>(R.id.btn_adding_port)
//        addingHostButton = dialogHosts.findViewById<Button>(R.id.btn_adding_host)
        val window = dialog.window
//        val windowHost = dialogHosts.window
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

    }
    private fun addNowButonListener() {
        nowButton!!.setOnClickListener {
            nowButton!!.isEnabled = false
            listener.sendMessage("treehouses tor notice now")
        }
    }

    private fun setWindowProperties(dialog: Dialog) {
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val window = dialog.window
        window!!.setGravity(Gravity.CENTER)
        window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun initializeProperties() {
//        bind!!.btnAddPort
//        startButton = bind!!.btnTorStart
//        addPortButton = bind!!.btnAddPort
//        startButton!!.isEnabled = false
//        startButton!!.text = "Getting Tor Status from raspberry pi"
//        textStatus = bind!!.torStatusText
//        textStatus!!.text = "-"
    }

    private fun addNotificationListener() {
        notification!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) listener.sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE_ON))
            else listener.sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE_OFF))
            notification!!.isEnabled = false
        }
    }

    private fun addTextStatusListener() {
        textStatus!!.setOnClickListener {
            myClipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            textStatus!!.text.toString()
            myClip = ClipData.newPlainText("text", textStatus!!.text)
            myClipboard!!.setPrimaryClip(myClip!!)
            Toast.makeText(requireContext(), textStatus!!.text.toString() + " copied!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addPortListListener() {
        portList!!.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
//            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
//            builder.setTitle("Delete Port " + portsName!![position] + " ?")
////            builder.setMessage("Would you like to delete?");
//
//            // add the buttons
//            builder.setPositiveButton("Confirm") { dialog, _ ->
//                listener.sendMessage(getString(R.string.TREEHOUSES_TOR_DELETE, portsName!![position].split(":".toRegex(), 2).toTypedArray()[0]))
//                addPortButton!!.text = "deleting port ....."
//                portList!!.isEnabled = false
//                addPortButton!!.isEnabled = false
//                dialog.dismiss()
//            }
//            builder.setNegativeButton("Cancel", null)
//
//            // create and show the alert dialog
//            val dialog = builder.create()
//            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
//            dialog.show()
        }
    }

    private fun addProfileButtonListeners(dialog: Dialog) {
//        val inputExternal: TextInputEditText = dialog.findViewById(R.id.ExternalTextInput)
//        val inputInternal: TextInputEditText = dialog.findViewById(R.id.InternalTextInput)

        addProfileButton!!.setOnClickListener {
//            inputExternal.clearFocus()
//            inputInternal.clearFocus()
            dialog.show()
        }

        addingProfileButton!!.setOnClickListener {
            if (serverHost.text.toString().isNotEmpty() && localAddress.text.toString().isNotEmpty() && localPort.text.toString().isNotEmpty() && serverPort.text.toString().isNotEmpty() && password.text.toString().isNotEmpty()) {
                val s1 = serverHost.text.toString()
                val s2 = localAddress.text.toString()
                val s3 = localPort.text.toString()
                val s4 = serverPort.text.toString()
                val s5 = password.text.toString()

                val message = "treehouses shadowsocks add { \\\"server\\\": \\\"$s1\\\", \\\"local_address\\\": \\\"$s2\\\", \\\"local_port\\\": $s3, \\\"server_port\\\": $s4, \\\"password\\\": \\\"$s5\\\", \\\"method\\\": \\\"rc4-md5\\\" }"
                listener.sendMessage(message)
                addProfileButton!!.text = "Adding......"
                addProfileButton!!.isEnabled = false
                dialog.dismiss()
            }
        }

//        val addingPortButton = dialog.findViewById<Button>(R.id.btn_adding_port)
//        addingPortButton.setOnClickListener {
//            dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
//            if (inputExternal.text.toString() !== "" && inputInternal.text.toString() !== "") {
//                val s1 = inputInternal.text.toString()
//                val s2 = inputExternal.text.toString()
//                listener.sendMessage(getString(R.string.TREEHOUSES_TOR_ADD, s2, s1))
//                addPortButton!!.text = "Adding port, please wait for a while ............"
//                portList!!.isEnabled = false
//                addPortButton!!.isEnabled = false
//                dialog.dismiss()
//                inputInternal.text?.clear()
//                inputExternal.text?.clear()
//                dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
//            }
//        }
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
        if (msg.what == Constants.MESSAGE_READ) {
            val readMessage: String = msg.obj as String
            Log.d("Socks reply", "" + readMessage)
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
            else if (readMessage.contains("tmptmp")){

                if(readMessage.contains(' '))
                    profileName!!.add(readMessage.split(' ')[0])
                else
                    profileName!!.add(readMessage)

                adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, profileName!!)
                bind!!.profiles.adapter = adapter
            }


        }
    }
}
