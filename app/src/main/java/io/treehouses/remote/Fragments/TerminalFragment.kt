package io.treehouses.remote.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.ExpandableListView.OnChildClickListener
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import io.treehouses.remote.Constants
import io.treehouses.remote.Fragments.DialogFragments.AddCommandDialogFragment
import io.treehouses.remote.Fragments.DialogFragments.ChPasswordDialogFragment
import io.treehouses.remote.MainApplication.Companion.commandList
import io.treehouses.remote.MainApplication.Companion.terminalList
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.adapter.CommandListAdapter
import io.treehouses.remote.bases.BaseTerminalFragment
import io.treehouses.remote.pojo.CommandListItem
import io.treehouses.remote.pojo.CommandsList
import io.treehouses.remote.utils.SaveUtils.getCommandsList
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class TerminalFragment : BaseTerminalFragment() {
    private var mConversationView: ListView? = null
    private var mPingStatus: TextView? = null
    private var mOutEditText: AutoCompleteTextView? = null
    private var mSendButton: Button? = null
    private var pingStatusButton: Button? = null
    private var mPrevious: Button? = null
    private var expandableListView: ExpandableListView? = null
    private var expandableListAdapter: ExpandableListAdapter? = null
    private var list: ArrayList<String>? = null
    private var commands: CommandsList? = null
    private var i = 0
    private var last: String? = null
    var view: View? = null
    private var expandableListTitle: List<String>? = null
    private var expandableListDetail: HashMap<String, List<CommandListItem>>? = null

    /**
     * Array adapter for the conversation thread
     */
    private var mConversationArrayAdapter: ArrayAdapter<String?>? = null

    /**
     * Member object for the chat services
     */
    private override var mChatService: BluetoothChatService? = null
    private var jsonSent = false
    private var jsonReceiving = false
    private var jsonString = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.activity_terminal_fragment, container, false)
        mChatService = listener.chatService
        mChatService!!.updateHandler(mHandler)
        jsonSent = true
        listener.sendMessage("treehouses remote commands json\n")
        instance = this
        expandableListDetail = HashMap()
        expandableListDetail!![TITLE_EXPANDABLE] = getCommandsList(context!!)
        Log.e("TERMINAL mChatService", "" + mChatService!!.state)
        setHasOptionsMenu(true)
        setupList()
        return view
    }

    fun setupList() {
        expandableListView = view!!.findViewById(R.id.terminalList)
        expandableListTitle = ArrayList(expandableListDetail!!.keys)
        expandableListAdapter = CommandListAdapter(context!!, expandableListTitle, expandableListDetail!!)
        expandableListView.setAdapter(expandableListAdapter)
        expandableListView.setOnChildClickListener(OnChildClickListener { parent: ExpandableListView?, v: View?, groupPosition: Int, childPosition: Int, id: Long ->
            if (childPosition < expandableListDetail!!["Commands"]!!.size) {
                val title = expandableListDetail!![expandableListTitle.get(groupPosition)]!![childPosition].title
                if (title.equals("CLEAR", ignoreCase = true)) {
                    terminalList!!.clear()
                    getmConversationArrayAdapter()!!.notifyDataSetChanged()
                } else if (title.equals("CHANGE PASSWORD", ignoreCase = true)) {
                    showDialog(ChPasswordDialogFragment.newInstance(), Constants.REQUEST_DIALOG_FRAGMENT_CHPASS, "ChangePassDialog")
                } else {
                    listener.sendMessage(expandableListDetail!![expandableListTitle.get(groupPosition)]!![childPosition].command)
                }
            } else {
                showDialog(AddCommandDialogFragment.newInstance(), Constants.REQUEST_DIALOG_FRAGMENT_ADD_COMMAND, "AddCommandDialog")
            }
            false
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mConversationView = view.findViewById(R.id.`in`)
        mOutEditText = view.findViewById(R.id.edit_text_out)
        setUpAutoComplete(mOutEditText)
        mSendButton = view.findViewById(R.id.button_send)
        mPingStatus = view.findViewById(R.id.pingStatus)
        pingStatusButton = view.findViewById(R.id.PING)
        mPrevious = view.findViewById(R.id.btnPrevious)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null && mChatService!!.state == Constants.STATE_NONE) {
            mChatService!!.start()
            idle(mPingStatus, pingStatusButton)
        }
    }

    override fun onResume() {
        checkStatus(mChatService, mPingStatus, pingStatusButton)
        super.onResume()
        setupChat()
    }

    private fun getmConversationArrayAdapter(): ArrayAdapter<String?>? {
        return mConversationArrayAdapter
    }

    /**
     * Set up the UI and background operations for chat.
     */
    override fun setupChat() {
        copyToList(mConversationView, context)
        mConversationArrayAdapter = object : ArrayAdapter<String?>(requireActivity(), R.layout.message, terminalList as List<String?>?) {
            override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
                return getViews(super.getView(position, convertView, parent), isRead)
            }
        }
        mConversationView!!.adapter = mConversationArrayAdapter
        btnSendClickListener()

        // Initialize the BluetoothChatService to perform bluetooth connections
        if (mChatService!!.state == Constants.STATE_NONE) mChatService = BluetoothChatService(mHandler, activity!!.applicationContext)
    }

    private fun btnSendClickListener() {
        // Initialize the send button with a listener that for click events
        mSendButton!!.setOnClickListener { v: View? ->
            // Send a message using content of the edit text widget
            val view = getView()
            if (null != view) {
                val consoleInput = view.findViewById<TextView>(R.id.edit_text_out)
                listener.sendMessage(consoleInput.text.toString())
                consoleInput.text = ""
            }
        }
        mPrevious!!.setOnClickListener { v: View? ->
            try {
                last = list!![--i]
                mOutEditText!!.setText(last)
                mOutEditText!!.setSelection(mOutEditText!!.length())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Constants.REQUEST_ENABLE_BT -> onResultCaseEnable(resultCode)
            Constants.REQUEST_DIALOG_FRAGMENT_CHPASS -> onResultCaseDialogChpass(resultCode, data)
            Constants.REQUEST_DIALOG_FRAGMENT_ADD_COMMAND -> if (resultCode == Activity.RESULT_OK) {
                expandableListDetail!!.clear()
                expandableListDetail!![TITLE_EXPANDABLE] = getCommandsList(context!!)
                expandableListTitle = ArrayList(expandableListDetail!!.keys)
                expandableListAdapter = CommandListAdapter(context!!, expandableListTitle, expandableListDetail!!)
                expandableListView!!.setAdapter(expandableListAdapter)
                expandableListView!!.expandGroup(0, true)
            }
        }
    }

    protected fun onResultCaseEnable(resultCode: Int) {
        // When the request to enable Bluetooth returns
        if (resultCode == Activity.RESULT_OK) {
            // Bluetooth is now enabled, so set up a chat session
            setupChat()
        } else {
            // User did not enable Bluetooth or an error occurred
            Toast.makeText(activity, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show()
            activity!!.finish()
        }
    }

    private fun showDialog(dialogFrag: DialogFragment, requestCode: Int, tag: String) {
        // Create an instance of the dialog fragment and show it
        dialogFrag.setTargetFragment(this, requestCode)
        dialogFrag.show(fragmentManager!!.beginTransaction(), tag)
    }

    private fun addToCommandList(writeMessage: String) {
        commandList!!.add(writeMessage)
        list = commandList
        i = list!!.size
    }

    private fun onResultCaseDialogChpass(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            //get password change request
            val chPWD = if (data!!.getStringExtra("password") == null) "" else data.getStringExtra("password")
            //store password and command
            //send password to command line interface
            listener.sendMessage("treehouses password $chPWD")
        }
    }

    private fun buildJSON() {
        try {
            val jsonObject = JSONObject(jsonString)
            commands = Gson().fromJson(jsonObject.toString(), CommandsList::class.java)
            if (commands != null) updateArrayAdapters(commands)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun handleJson(readMessage: String) {
        if (jsonReceiving) {
            jsonString += readMessage.trim { it <= ' ' }
            if (jsonString.endsWith("]}")) {
                jsonString += readMessage.trim { it <= ' ' }
                buildJSON()
                jsonReceiving = false
                jsonSent = false
            }
        } else if (readMessage.startsWith("{")) {
            jsonReceiving = true
            jsonString = readMessage.trim { it <= ' ' }
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_STATE_CHANGE -> if (msg.arg1 == Constants.STATE_LISTEN || msg.arg1 == Constants.STATE_NONE) {
                    idle(mPingStatus, pingStatusButton)
                }
                Constants.MESSAGE_WRITE -> {
                    isRead = false
                    addToCommandList(handlerCaseWrite(TAG, mConversationArrayAdapter, msg))
                }
                Constants.MESSAGE_READ -> {
                    val readMessage = msg.obj as String
                    isRead = true
                    if (readMessage.contains("unknown")) jsonSent = false
                    if (jsonSent) handleJson(readMessage) else {
                        handlerCaseRead(readMessage, mPingStatus, pingStatusButton)
                        filterMessages(readMessage, mConversationArrayAdapter, terminalList)
                    }
                }
                Constants.MESSAGE_DEVICE_NAME -> handlerCaseName(msg, activity)
                Constants.MESSAGE_TOAST -> handlerCaseToast(msg)
            }
        }
    }

    companion object {
        private const val TAG = "BluetoothChatFragment"
        private const val TITLE_EXPANDABLE = "Commands"
        var instance: TerminalFragment? = null
            private set
        private var isRead = false
    }
}