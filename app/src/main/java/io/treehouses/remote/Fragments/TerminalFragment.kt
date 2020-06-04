package io.treehouses.remote.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.Toast
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
import io.treehouses.remote.databinding.ActivityTerminalFragmentBinding
import io.treehouses.remote.pojo.CommandListItem
import io.treehouses.remote.pojo.CommandsList
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.SaveUtils
import io.treehouses.remote.utils.match
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class TerminalFragment : BaseTerminalFragment() {
    private lateinit var expandableListAdapter: ExpandableListAdapter
    private lateinit var list: ArrayList<String>
    private lateinit var commands: CommandsList
    private var i = 0
    private lateinit var last: String
    private lateinit var expandableListTitle: List<String>
    private lateinit var expandableListDetail: HashMap<String, List<CommandListItem>>

    /**
     * Array adapter for the conversation thread
     */
    private var mConversationArrayAdapter: ArrayAdapter<String>? = null

    /**
     * Member object for the chat services
     */
    private var jsonSent = false
    private var jsonReceiving = false
    private var jsonString = ""

    private lateinit var bind: ActivityTerminalFragmentBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityTerminalFragmentBinding.inflate(inflater, container, false)
        onLoad(mHandler)
        jsonSent = true
        listener.sendMessage(getString(R.string.TREEHOUSES_COMMANDS_JSON))
        instance = this
        expandableListDetail = HashMap()
        expandableListDetail[TITLE_EXPANDABLE] = SaveUtils.getCommandsList(context)
        setHasOptionsMenu(true)
        setupList()
        return bind.root
    }

    fun setupList() {
        expandableListTitle = ArrayList(expandableListDetail.keys)
        expandableListAdapter = CommandListAdapter(context, expandableListTitle, expandableListDetail)
        bind.terminalList.setAdapter(expandableListAdapter)
        bind.terminalList.setOnChildClickListener { _: ExpandableListView?, _: View?, groupPosition: Int, childPosition: Int, _: Long ->
            if (childPosition < expandableListDetail["Commands"]!!.size) {
                val title = expandableListDetail[expandableListTitle[groupPosition]]!![childPosition].title
                when {
                    title.equals("CLEAR", ignoreCase = true) -> {
                        terminalList!!.clear()
                        getmConversationArrayAdapter()!!.notifyDataSetChanged()
                    }
                    title.equals("CHANGE PASSWORD", ignoreCase = true) -> showDialog(ChPasswordDialogFragment.newInstance(), Constants.REQUEST_DIALOG_FRAGMENT_CHPASS, "ChangePassDialog")
                    else -> listener.sendMessage(expandableListDetail[expandableListTitle[groupPosition]]!![childPosition].command)
                }
            } else {
                showDialog(AddCommandDialogFragment.newInstance(), Constants.REQUEST_DIALOG_FRAGMENT_ADD_COMMAND, "AddCommandDialog")
            }
            false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpAutoComplete(bind.editTextOut)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService.state == Constants.STATE_NONE) {
            mChatService.start()
            updatePingStatus(bind.pingStatus, bind.PING, getString(R.string.bStatusIdle), Color.YELLOW)
        }
    }

    override fun onResume() {
        checkStatus(mChatService, bind.pingStatus, bind.PING)
        super.onResume()
        setupChat()
    }

    private fun getmConversationArrayAdapter(): ArrayAdapter<String>? {
        return mConversationArrayAdapter
    }

    /**
     * Set up the UI and background operations for chat.
     */
    override fun setupChat() {
        copyToList(bind.`in`, context)
        mConversationArrayAdapter = object : ArrayAdapter<String>(requireActivity(), R.layout.message, terminalList!!) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getViews(super.getView(position, convertView, parent), isRead)
            }
        }
        bind.`in`.adapter = mConversationArrayAdapter
        btnSendClickListener()

        // Initialize the BluetoothChatService to perform bluetooth connections
        if (mChatService.state == Constants.STATE_NONE) mChatService = BluetoothChatService(mHandler, requireActivity().applicationContext)
    }

    private fun btnSendClickListener() {
        // Initialize the send button with a listener that for click events
        bind.buttonSend.setOnClickListener { v: View? ->
            // Send a message using content of the edit text widget
            val view = view
            if (null != view) {
                listener.sendMessage(bind.editTextOut.text.toString())
                bind.editTextOut.setText("")
            }
        }
        bind.btnPrevious.setOnClickListener { v: View? ->
            try {
                last = list[--i]
                bind.editTextOut.setText(last)
                bind.editTextOut.setSelection(bind.editTextOut.length())
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
                expandableListDetail.clear()
                expandableListDetail[TITLE_EXPANDABLE] = SaveUtils.getCommandsList(context)
                expandableListTitle = ArrayList(expandableListDetail.keys)
                expandableListAdapter = CommandListAdapter(context, expandableListTitle, expandableListDetail)
                bind.terminalList.setAdapter(expandableListAdapter)
                bind.terminalList.expandGroup(0, true)
            }
        }
    }

    private fun onResultCaseEnable(resultCode: Int) {
        // When the request to enable Bluetooth returns
        if (resultCode == Activity.RESULT_OK) {
            // Bluetooth is now enabled, so set up a chat session
            setupChat()
        } else {
            // User did not enable Bluetooth or an error occurred
            Toast.makeText(activity, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

    private fun showDialog(dialogFrag: DialogFragment, requestCode: Int, tag: String) {
        // Create an instance of the dialog fragment and show it
        dialogFrag.setTargetFragment(this, requestCode)
        dialogFrag.show(requireActivity().supportFragmentManager.beginTransaction(), tag)
    }

    private fun addToCommandList(writeMessage: String) {
        commandList.add(writeMessage)
        list = commandList
        i = list.size
    }

    private fun onResultCaseDialogChpass(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            //get password change request
            val chPWD = if (data!!.getStringExtra("password") == null) "" else data.getStringExtra("password")
            //store password and command
            //send password to command line interface
            listener.sendMessage(getString(R.string.TREEHOUSES_PASSWORD, chPWD))
        }
    }

    private fun buildJSON() {
        try {
            val jsonObject = JSONObject(jsonString)
            commands = Gson().fromJson(jsonObject.toString(), CommandsList::class.java)
            updateArrayAdapters(commands)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun handleJson(readMessage: String) {
        val s = match(readMessage)
        if (jsonReceiving) {
            jsonString += readMessage.trim()
            if (s == RESULTS.END_JSON_COMMANDS) {
                jsonString += readMessage.trim()
                buildJSON()
                jsonReceiving = false
                jsonSent = false
            }
        } else if (jsonSent && s == RESULTS.START_JSON) {
            jsonReceiving = true
            jsonString = readMessage.trim()
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_STATE_CHANGE -> checkStatus(mChatService, bind.pingStatus, bind.PING)
                Constants.MESSAGE_WRITE -> {
                    isRead = false
                    addToCommandList(handlerCaseWrite(TAG, mConversationArrayAdapter, msg))
                }
                Constants.MESSAGE_READ -> {
                    val readMessage = msg.obj as String
                    val s = match(readMessage)
                    isRead = true
                    if (s == RESULTS.ERROR) jsonSent = false
                    if (jsonSent) handleJson(readMessage) else {
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