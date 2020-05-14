package io.treehouses.remote.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.treehouses.remote.Constants
import io.treehouses.remote.MainApplication.Companion.tunnelList
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseTerminalFragment
import java.util.*

class TunnelFragment : BaseTerminalFragment() {
    private var mPingStatus: TextView? = null
    private var pingStatusButton: Button? = null
    private var mConversationArrayAdapter: ArrayAdapter<String?>? = null
    private var mConversationView: ListView? = null
    private var aSwitch: Switch? = null
    var view: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.activity_tunnel_fragment, container, false)
        val listview = ArrayList<String>()
        val terminallist = view.findViewById<ListView>(R.id.list_command)
        terminallist.divider = null
        terminallist.dividerHeight = 0
        val adapter = ArrayAdapter(Objects.requireNonNull(activity), R.layout.tunnel_commands_list, R.id.command_textView, listview)
        terminallist.adapter = adapter
        aSwitch = view.findViewById(R.id.switchNotification)
        onSwitchChecked()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mConversationView = view.findViewById(R.id.list_command)
        mPingStatus = view.findViewById(R.id.pingStatus)
        pingStatusButton = view.findViewById(R.id.PING)
        val btn_start = view.findViewById<Button>(R.id.btn_start_config)
        val btn_execute_start = view.findViewById<Button>(R.id.btn_execute_start)
        val btn_execute_stop = view.findViewById<Button>(R.id.btn_execute_stop)
        val btn_execute_destroy = view.findViewById<Button>(R.id.btn_execute_destroy)
        val btn_execute_address = view.findViewById<Button>(R.id.btn_execute_address)
        btn_start.setOnClickListener { v: View? -> listener.sendMessage("treehouses tor") }
        btn_execute_address.setOnClickListener { v: View? -> listener.sendMessage("treehouses tor") }
        btn_execute_start.setOnClickListener { v: View? -> listener.sendMessage("treehouses tor start") }
        btn_execute_stop.setOnClickListener { v: View? -> listener.sendMessage("treehouses tor stop") }
        btn_execute_destroy.setOnClickListener { v: View? -> listener.sendMessage("treehouses tor destroy") }
    }

    override fun onStart() {
        super.onStart()
        onLoad(mHandler)
    }

    override fun setupChat() {
        Log.e("tag", "LOG setupChat()")
        val inflater = layoutInflater
        // Initialize the array adapter for the conversation thread
        viewFunction
        copyToList(mConversationView, context)
        mConversationView!!.adapter = mConversationArrayAdapter

        // Initialize the BluetoothChatService to perform bluetooth connections
        if (mChatService.state == Constants.STATE_NONE) {
            mChatService = BluetoothChatService(mHandler, activity!!.applicationContext)
        }
        // Initialize the buffer for outgoing messages
        StringBuilder()
    }

    private val viewFunction: Unit
        private get() {
            mConversationArrayAdapter = object : ArrayAdapter<String?>(Objects.requireNonNull(activity), R.layout.message, tunnelList) {
                override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
                    var view = super.getView(position, convertView, parent)
                    view = getViews(view, isRead)
                    return view
                }
            }
        }

    private fun onSwitchChecked() {
        aSwitch!!.setOnClickListener { v: View? ->
            if (aSwitch!!.isChecked) {
                listener.sendMessage("treehouses tor notice on")
            } else {
                listener.sendMessage("treehouses tor notice off")
            }
        }
    }

    private fun configConditions(readMessage: String) {
        if (readMessage.trim { it <= ' ' }.contains("Error")) {
            try {
                listener.sendMessage("treehouses tor start")
                Thread.sleep(300)
                listener.sendMessage("treehouses tor add 80")
                Thread.sleep(300)
                listener.sendMessage("treehouses tor add 22")
                Thread.sleep(300)
                listener.sendMessage("treehouses tor add 2200")
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_STATE_CHANGE -> if (msg.arg1 == Constants.STATE_LISTEN || msg.arg1 == Constants.STATE_NONE) {
                    idle(mPingStatus, pingStatusButton)
                }
                Constants.MESSAGE_WRITE -> {
                    isRead = false
                    handlerCaseWrite(TAG, mConversationArrayAdapter, msg)
                }
                Constants.MESSAGE_READ -> {
                    val readMessage = msg.obj as String
                    isRead = true
                    configConditions(readMessage)
                    handlerCaseRead(readMessage, mPingStatus, pingStatusButton)
                    filterMessages(readMessage, mConversationArrayAdapter, tunnelList)
                }
                Constants.MESSAGE_DEVICE_NAME -> {
                    val activity: Activity? = activity
                    handlerCaseName(msg, activity)
                }
                Constants.MESSAGE_TOAST -> handlerCaseToast(msg)
            }
        }
    }

    override fun onResume() {
        Log.e("CHECK STATUS", "" + mChatService.state)
        checkStatus(mChatService, mPingStatus, pingStatusButton)
        super.onResume()
    }

    companion object {
        private const val TAG = "BluetoothChatFragment"
        private var isRead = false
    }
}