package io.treehouses.remote.Fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import io.treehouses.remote.Constants
import io.treehouses.remote.Fragments.DialogFragments.AddCommandDialogFragment
import io.treehouses.remote.Fragments.DialogFragments.ChPasswordDialogFragment
import io.treehouses.remote.MainApplication.Companion.commandList
import io.treehouses.remote.MainApplication.Companion.terminalList
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.adapter.CommandListAdapter
import io.treehouses.remote.bases.BaseTerminalFragment
import io.treehouses.remote.databinding.ActivityTerminalFragmentBinding
import io.treehouses.remote.pojo.CommandListItem
import io.treehouses.remote.ui.home.HomeFragment
import io.treehouses.remote.utils.SaveUtils
import io.treehouses.remote.utils.match
import java.util.*

class TerminalFragment : BaseTerminalFragment() {
    private lateinit var expandableListAdapter: ExpandableListAdapter
    private var i = 0
    private lateinit var expandableListTitle: List<String>
    private lateinit var expandableListDetail: HashMap<String, List<CommandListItem>>

    /**
     * Array adapter for the conversation thread
     */
    private var mConversationArrayAdapter: ArrayAdapter<String>? = null

    /**
     * Member object for the chat services
     */

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityTerminalFragmentBinding.inflate(inflater, container, false)
        onLoad(mHandler)
        val autocomplete = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("autocomplete", true)
        if (autocomplete) {
            jsonSend(true)
            listener.sendMessage(getString(R.string.TREEHOUSES_COMMANDS_JSON))
        }
        instance = this
        expandableListDetail = HashMap()
        expandableListDetail[TITLE_EXPANDABLE] = SaveUtils.getCommandsList(requireContext())
        setHasOptionsMenu(true)
        bind.treehousesBtn.text = null;
        bind.treehousesBtn.textOn = null;
        bind.treehousesBtn.textOff = null;
        return bind.root
    }

    private fun setupList() {
        expandableListTitle = ArrayList(expandableListDetail.keys)
        expandableListAdapter = CommandListAdapter(requireContext(), expandableListTitle, expandableListDetail)
        bind.terminalList.setAdapter(expandableListAdapter)
        bind.terminalList.setOnChildClickListener { _: ExpandableListView?, _: View?, groupPosition: Int, childPosition: Int, _: Long ->
            if (childPosition < expandableListDetail["Commands"]!!.size) {
                val title = expandableListDetail[expandableListTitle[groupPosition]]!![childPosition].getTitle()
                when {
                    title.equals("CLEAR", ignoreCase = true) -> {
                        terminalList!!.clear()
                        getmConversationArrayAdapter()!!.notifyDataSetChanged()
                    }
                    title.equals("CHANGE PASSWORD", ignoreCase = true) -> showDialog(ChPasswordDialogFragment.newInstance(), Constants.REQUEST_DIALOG_FRAGMENT_CHPASS, "ChangePassDialog")
                    else -> listener.sendMessage(expandableListDetail[expandableListTitle[groupPosition]]!![childPosition].getCommand())
                }
            } else {
                showDialog(AddCommandDialogFragment.newInstance(), Constants.REQUEST_DIALOG_FRAGMENT_ADD_COMMAND, "AddCommandDialog")
            }
            false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupList()
        setUpAutoComplete(bind.editTextOut)
        Tutorials.terminalTutorials(bind, requireActivity())
    }

    override fun onDestroy() {
        super.onDestroy()
        if(chatOpen()) {
            if (mChatService.state == Constants.STATE_NONE) {
                mChatService.start()
                updatePingStatus(bind.pingStatus, bind.PING, getString(R.string.bStatusIdle), Color.YELLOW)
            }
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
        copyToList(bind.`in`)
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

    private fun sendMessage() {
        // Send a message using content of the edit text widget
        val view = view
        if (null != view) {
            listener.sendMessage(bind.editTextOut.text.toString())
            if(bind.editTextOut.text.toString() == "reboot") {
                Thread.sleep(1000)
                listener.openCallFragment(HomeFragment())
                Toast.makeText(context,"Bluetooth Disconnected: Reboot in progress", Toast.LENGTH_LONG).show()
                requireActivity().title = "Home"
            }
            checkIfTreehouses()
        }
    }

    private fun getInfo() {
        when {
            jsonSent -> Toast.makeText(context, "Please Wait", Toast.LENGTH_SHORT).show()
            helpJsonString.isNotEmpty() -> showHelpDialog(helpJsonString)
            else -> {
                jsonSend(true)
                listener.sendMessage(getString(R.string.TREEHOUSES_HELP_JSON))
            }
        }
    }

    private fun getPreviousCommand() {
        try {
            bind.editTextOut.setText(commandList[--i].trim())
            bind.editTextOut.setSelection(bind.editTextOut.length())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun btnSendClickListener() {
        // Initialize the send button with a listener that for click events
        bind.buttonSend.setOnClickListener {
            sendMessage()
        }
        bind.btnPrevious.setOnClickListener {
            getPreviousCommand()
        }
        bind.infoButton.setOnClickListener {
            getInfo()
        }
        bind.treehousesBtn.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
           treehouses = isChecked
           checkIfTreehouses()
        }
        bind.editTextOut.addTextChangedListener {
            if (TextUtils.isEmpty(it.toString().trim())) bind.treehousesBtn.isChecked= false
        }
    }

    private fun checkIfTreehouses() {
        if(treehouses) {
            bind.editTextOut.setText("treehouses ")
            bind.editTextOut.setSelection(bind.editTextOut.text.length)
        }
        else {
            bind.editTextOut.setText("")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Constants.REQUEST_ENABLE_BT -> onResultCaseEnable(resultCode)
            Constants.REQUEST_DIALOG_FRAGMENT_CHPASS -> {
                if (resultCode == Activity.RESULT_OK) {
                    val chPWD = if (data!!.getStringExtra("password") == null) "" else data.getStringExtra("password")
                    listener.sendMessage(getString(R.string.TREEHOUSES_PASSWORD, chPWD))
                }
            }
            Constants.REQUEST_DIALOG_FRAGMENT_ADD_COMMAND -> if (resultCode == Activity.RESULT_OK) {
                expandableListDetail.clear()
                expandableListDetail[TITLE_EXPANDABLE] = SaveUtils.getCommandsList(requireContext())
                expandableListTitle = ArrayList(expandableListDetail.keys)
                expandableListAdapter = CommandListAdapter(requireContext(), expandableListTitle, expandableListDetail)
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

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    override fun getMessage(msg: Message) {
        when (msg.what) {
            Constants.MESSAGE_STATE_CHANGE -> checkStatus(mChatService, bind.pingStatus, bind.PING)
            Constants.MESSAGE_WRITE -> {
                isRead = false
                commandList.add(handlerCaseWrite(mConversationArrayAdapter, msg))
                i = commandList.size
            }
            Constants.MESSAGE_READ -> {
                val readMessage = msg.obj as String
                isRead = true
                if (readMessage.contains("unknown")) jsonSend(false)
                if (jsonSent) handleJson(readMessage)
                else {
                    filterMessages(readMessage, mConversationArrayAdapter, terminalList)
                }
            }
            Constants.MESSAGE_DEVICE_NAME -> handlerCaseName(msg, activity)
            Constants.MESSAGE_TOAST -> handlerCaseToast(msg)
        }
    }

    companion object {
        var treehouses: Boolean = false
        private const val TAG = "BluetoothChatFragment"
        private const val TITLE_EXPANDABLE = "Commands"
        var instance: TerminalFragment? = null
            private set
        private var isRead = false
    }
}