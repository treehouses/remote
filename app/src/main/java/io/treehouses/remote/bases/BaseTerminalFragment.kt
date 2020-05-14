package io.treehouses.remote.bases

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.preference.PreferenceManager
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.pojo.CommandsList
import io.treehouses.remote.utils.Utils
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class BaseTerminalFragment : BaseFragment() {
    private val array2 = arrayOf("treehouses", "docker")
    private var inSecondLevel: MutableSet<String>? = null
    private var inThirdLevel: MutableSet<String>? = null
    private var arrayAdapter1: ArrayAdapter<String>? = null
    private var arrayAdapter2: ArrayAdapter<String>? = null
    private var arrayAdapter3: ArrayAdapter<String>? = null
    fun handlerCaseWrite(TAG: String?, mConversationArrayAdapter: ArrayAdapter<String?>, msg: Message): String {
        val writeBuf = msg.obj as ByteArray
        // construct a string from the buffer
        val writeMessage = String(writeBuf)
        if (!writeMessage.contains("google.com") && !writeMessage.contains("remote")) {
            Log.d(TAG, "writeMessage = $writeMessage")
            mConversationArrayAdapter.add("\nCommand:  $writeMessage")
        }
        return writeMessage
    }

    fun handlerCaseName(msg: Message, activity: Activity?) {
        // save the connected device's name
        val mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)
        if (null != activity) {
            Toast.makeText(activity, "Connected to $mConnectedDeviceName", Toast.LENGTH_SHORT).show()
        }
    }

    fun handlerCaseToast(msg: Message) {
        if (null != activity) {
            Toast.makeText(activity, msg.data.getString(Constants.TOAST), Toast.LENGTH_SHORT).show()
        }
    }

    fun getViews(view: View, isRead: Boolean): View {
        val consoleView = view.findViewById<TextView>(R.id.listItem)
        if (isRead) {
            consoleView.setTextColor(Color.BLUE)
        } else {
            consoleView.setTextColor(Color.RED)
        }
        return view
    }

    private fun bgResource(pingStatusButton: Button, color: Int) {
        pingStatusButton.setBackgroundResource(R.drawable.circle)
        val bgShape = pingStatusButton.background as GradientDrawable
        bgShape.setColor(color)
    }

    private fun offline(mPingStatus: TextView, pingStatusButton: Button) {
        mPingStatus.setText(R.string.bStatusOffline)
        bgResource(pingStatusButton, Color.RED)
    }

    protected fun idle(mPingStatus: TextView, pingStatusButton: Button) {
        mPingStatus.setText(R.string.bStatusIdle)
        bgResource(pingStatusButton, Color.YELLOW)
    }

    private fun connect(mPingStatus: TextView, pingStatusButton: Button) {
        mPingStatus.setText(R.string.bStatusConnected)
        bgResource(pingStatusButton, Color.GREEN)
    }

    protected fun copyToList(mConversationView: ListView, context: Context?) {
        mConversationView.onItemClickListener = OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            val clickedData = mConversationView.getItemAtPosition(position) as String
            Utils.copyToClipboard(context, clickedData)
        }
    }

    protected fun checkStatus(mChatService: BluetoothChatService, mPingStatus: TextView, pingStatusButton: Button) {
        if (mChatService.getState() === Constants.STATE_CONNECTED) {
            connect(mPingStatus, pingStatusButton)
        } else if (mChatService.getState() === Constants.STATE_NONE) {
            offline(mPingStatus, pingStatusButton)
        } else {
            idle(mPingStatus, pingStatusButton)
        }
    }

    private fun filterMessage(readMessage: String): Boolean {
        val a = !readMessage.contains("1 packets") && !readMessage.contains("64 bytes") && !readMessage.contains("google.com") && !readMessage.contains("rtt") && !readMessage.trim { it <= ' ' }.isEmpty()
        val b = !readMessage.startsWith("treehouses ") && !readMessage.contains("treehouses remote commands")
        return a && b
    }

    protected fun filterMessages(readMessage: String, mConversationArrayAdapter: ArrayAdapter<*>, list: ArrayList<*>) {
        //make it so text doesn't show on chat (need a better way to check multiple strings since mConversationArrayAdapter only takes messages line by line)
        if (filterMessage(readMessage)) {
            list.add(readMessage)
            mConversationArrayAdapter.notifyDataSetChanged()
        }
    }

    private fun isPingSuccesfull(readMessage: String, mPingStatus: TextView, pingStatusButton: Button) {
        var readMessage = readMessage
        readMessage = readMessage.trim { it <= ' ' }

        //check if ping was successful
        if (readMessage.contains("1 packets")) {
            connect(mPingStatus, pingStatusButton)
        }
        if (readMessage.contains("Unreachable") || readMessage.contains("failure")) {
            offline(mPingStatus, pingStatusButton)
        }
    }

    protected fun handlerCaseRead(readMessage: String, mPingStatus: TextView, pingStatusButton: Button) {
        Log.d("TAG", "readMessage = $readMessage")

        //TODO: if message is json -> callback from RPi
        if (!isJson(readMessage)) {
            isPingSuccesfull(readMessage, mPingStatus, pingStatusButton)
        }
    }

    private fun isJson(readMessage: String): Boolean {
        try {
            JSONObject(readMessage)
        } catch (ex: JSONException) {
            return false
        }
        return true
    }

    private fun countSpaces(s: String): Int {
        var count = 0
        for (i in 0 until s.length) {
            if (s[i] == ' ') count++
        }
        return count
    }

    protected fun setUpAutoComplete(autoComplete: AutoCompleteTextView) {
        inSecondLevel = HashSet()
        inThirdLevel = HashSet()
        val preferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(context))
        arrayAdapter1 = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, array2)
        arrayAdapter2 = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, ArrayList())
        arrayAdapter3 = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, ArrayList())
        if (preferences.getBoolean("autocomplete", true)) {
            autoComplete.threshold = 0
            autoComplete.setAdapter(arrayAdapter1)
            addTextChangeListener(autoComplete)
            addSpaces(autoComplete)
        }
    }

    private fun addTextChangeListener(autoComplete: AutoCompleteTextView) {
        autoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (countSpaces(s.toString()) == 0) {
                    autoComplete.setAdapter(arrayAdapter1)
                } else if (countSpaces(s.toString()) == 1) {
                    autoComplete.setAdapter(arrayAdapter2)
                } else if (countSpaces(s.toString()) == 2) {
                    autoComplete.setAdapter(arrayAdapter3)
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().endsWith("\n")) {
                    listener.sendMessage(autoComplete.text.toString().substring(0, autoComplete.text.toString().length - 1))
                    autoComplete.setText("")
                }
            }
        })
    }

    private fun getRootCommand(s: String): String {
        val stringBuilder = StringBuilder()
        var count = 0
        for (i in 0 until s.length) {
            if (s[i] == ' ') count++
            if (count >= 2) break
            stringBuilder.append(s[i])
        }
        return stringBuilder.toString()
    }

    protected fun updateArrayAdapters(data: CommandsList) {
        if (data.commands == null) {
            Toast.makeText(requireContext(), "Error has occurred. Please Refresh", Toast.LENGTH_SHORT).show()
            return
        }
        for (i in 0 until data.commands.size()) {
            val s = getRootCommand(data.commands.get(i)).trim { it <= ' ' }
            Log.d("TAG", "updateArrayAdapters: $s")
            if (!inSecondLevel!!.contains(s)) {
                arrayAdapter2!!.add(s)
                inSecondLevel!!.add(s)
            }
            if (!inThirdLevel!!.contains(data.commands.get(i))) {
                arrayAdapter3!!.add(data.commands.get(i))
                inThirdLevel!!.add(data.commands.get(i))
            }
        }
    }

    private fun addSpaces(autoComplete: AutoCompleteTextView) {
        autoComplete.onItemClickListener = OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            autoComplete.postDelayed({ autoComplete.showDropDown() }, 100)
            autoComplete.append(" ")
        }
    }
}