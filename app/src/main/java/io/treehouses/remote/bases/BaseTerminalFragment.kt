package io.treehouses.remote.bases

import android.app.Activity
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
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.Utils.copyToClipboard
import io.treehouses.remote.utils.match
import java.util.*

open class BaseTerminalFragment : BaseFragment() {
    private val array2 = arrayOf("treehouses", "docker")
    private var inSecondLevel: MutableSet<String>? = null
    private var inThirdLevel: MutableSet<String>? = null
    private var arrayAdapter1: ArrayAdapter<String>? = null
    private var arrayAdapter2: ArrayAdapter<String>? = null
    private var arrayAdapter3: ArrayAdapter<String>? = null

    protected var jsonSent = false
    protected  var jsonReceiving = false

    fun handlerCaseWrite(TAG: String?, mConversationArrayAdapter: ArrayAdapter<String>?, msg: Message): String {
        val writeBuf = msg.obj as ByteArray
        // construct a string from the buffer
        val writeMessage = String(writeBuf)
        if (match(writeMessage) != RESULTS.PING_OUTPUT && !jsonSent) {
            Log.d(TAG, "writeMessage = $writeMessage")
            mConversationArrayAdapter?.add("\nCommand:  $writeMessage")
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
        if (isRead) { consoleView.setTextColor(resources.getColor(R.color.terminal))
        } else { consoleView.setTextColor(Color.RED) }
        return view
    }

    protected fun updatePingStatus(mPingStatus: TextView, pingStatusButton: Button, text: String, color: Int) {
        mPingStatus.text = text
        pingStatusButton.setBackgroundResource(R.drawable.circle)
        val bgShape = pingStatusButton.background as GradientDrawable
        bgShape.setColor(color)
    }

    protected fun copyToList(mConversationView: ListView) {
        mConversationView.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val clickedData = mConversationView.getItemAtPosition(position) as String
            requireContext().copyToClipboard( clickedData)
        }
    }

    protected fun checkStatus(mChatService: BluetoothChatService, mPingStatus: TextView, pingStatusButton: Button) {
        when (mChatService.state) {
            Constants.STATE_CONNECTED -> updatePingStatus(mPingStatus, pingStatusButton, getString(R.string.bStatusConnected), Color.GREEN)
            Constants.STATE_NONE -> updatePingStatus(mPingStatus, pingStatusButton, getString(R.string.bStatusOffline), Color.RED)
            else -> updatePingStatus(mPingStatus, pingStatusButton, getString(R.string.bStatusIdle), Color.YELLOW)
        }
    }

    private fun filterMessage(readMessage: String): Boolean {
        val a = !readMessage.contains("1 packets") && !readMessage.contains("64 bytes") && !readMessage.contains("google.com") && !readMessage.contains("rtt") && readMessage.trim { it <= ' ' }.isNotEmpty()
        val b = !readMessage.startsWith("treehouses ") && !readMessage.contains("treehouses remote commands") && !jsonSent
        return a && b
    }

    protected fun filterMessages(readMessage: String, mConversationArrayAdapter: ArrayAdapter<String>?, list: ArrayList<String>?) {
        //make it so text doesn't show on chat (need a better way to check multiple strings since mConversationArrayAdapter only takes messages line by line)
        if (filterMessage(readMessage)) {
            list?.add(readMessage)
            mConversationArrayAdapter?.notifyDataSetChanged()
        }
    }

    private fun countSpaces(s: String): Int {
        var count = 0
        for (element in s) if (element == ' ') count++
        return count
    }

    protected fun setUpAutoComplete(autoComplete: AutoCompleteTextView) {
        inSecondLevel = HashSet()
        inThirdLevel = HashSet()
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        arrayAdapter1 = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, array2)
        arrayAdapter2 = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, ArrayList())
        arrayAdapter3 = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, ArrayList())
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
                when {
                    countSpaces(s.toString()) == 0 -> autoComplete.setAdapter(arrayAdapter1)
                    countSpaces(s.toString()) == 1 -> autoComplete.setAdapter(arrayAdapter2)
                    countSpaces(s.toString()) == 2 -> autoComplete.setAdapter(arrayAdapter3)
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
        for (i in s.indices) {
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
        for (i in data.commands!!.indices) {
            val s = getRootCommand(data.commands!![i]).trim { it <= ' ' }
            Log.d("TAG", "updateArrayAdapters: $s")
            if (!inSecondLevel!!.contains(s)) {
                arrayAdapter2?.add(s)
                inSecondLevel?.add(s)
            }
            if (!inThirdLevel!!.contains(data.commands!![i])) {
                arrayAdapter3?.add(data.commands!![i])
                inThirdLevel?.add(data.commands!![i])
            }
        }
    }

    private fun addSpaces(autoComplete: AutoCompleteTextView) {
        autoComplete.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long ->
            autoComplete.postDelayed({ autoComplete.showDropDown() }, 100)
            autoComplete.append(" ")
        }
    }
}