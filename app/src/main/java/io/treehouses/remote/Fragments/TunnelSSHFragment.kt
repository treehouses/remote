package io.treehouses.remote.Fragments

import android.R
import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import io.treehouses.remote.Constants
import io.treehouses.remote.Fragments.DialogFragments.HelpDialog
import io.treehouses.remote.MainApplication
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.adapter.ViewHolderSSHKey
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityTunnelSshFragmentBinding
import io.treehouses.remote.pojo.CommandsList
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.match
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class TunnelSSHFragment : BaseFragment() {
    override lateinit var mChatService: BluetoothChatService

    protected var jsonReceiving = false
    protected var jsonSent = false
    private var portsName: ArrayList<String>? = null
    private var adapter: ArrayAdapter<String>? = null

    private var jsonString = ""



    var bind: ActivityTunnelSshFragmentBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityTunnelSshFragmentBinding.inflate(inflater, container, false)

        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)


        bind!!.btnGetKey.setOnClickListener {
            listener.sendMessage("treehouses remote key send")
            jsonSend(true)

        }

        bind!!.btnSendToPi.setOnClickListener{
            val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("SSHKeyPref", MODE_PRIVATE)
            val publicKey: String? = sharedPreferences.getString("public_key", "")
            val privateKey: String? = sharedPreferences.getString("private_key", "")
            listener.sendMessage("treehouses remote key receive $publicKey $privateKey")
        }

        bind!!.btnAddSshtunnelPort.setOnClickListener {
            val port: String = bind!!.editTextSSHTunnel.text.toString().trim()
            if(port != "" && port.matches("^[0-9]*$".toRegex())){
                Log.d("Port", bind!!.editTextSSHTunnel.text.toString())
                listener.sendMessage("treehouses sshtunnel add host $port")
            }
            else{
                Toast.makeText(requireContext(), "A numeric port interval is required", Toast.LENGTH_SHORT).show()
            }
        }

        bind!!.btnRemoveAll.setOnClickListener {
            listener.sendMessage("treehouses sshtunnel remove all")
        }
//
//        notification = bind!!.switchNotification
//        notification!!.isEnabled = false
//
        return bind!!.root
    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {

            when (msg.what) {
//                Constants.MESSAGE_STATE_CHANGE -> checkStatus(mChatService, bind.pingStatus, bind.PING)
//                Constants.MESSAGE_WRITE -> {
//                    TerminalFragment.isRead = false
//                    addToCommandList(handlerCaseWrite(TerminalFragment.TAG, mConversationArrayAdapter, msg))
//                }
                Constants.MESSAGE_READ -> {
                    val readMessage = msg.obj as String
                    match(readMessage)

                    if(readMessage.contains("treehouses tor portstreehouses remote key send")){
                        listener.sendMessage("treehouses remote key send")
                    }
                    if(readMessage.contains("key required")){
                        listener.sendMessage("treehouses remote key send")
                        Toast.makeText(requireContext(), "No keys saved, retrieving keys now", Toast.LENGTH_SHORT).show()
                    }
                    if(readMessage.contains("already exists")){
                        Toast.makeText(requireContext(), "Port already exists", Toast.LENGTH_SHORT).show()
                    }
                    if(readMessage.contains("Saved")){
                        Toast.makeText(requireContext(), "Keys successfully saved to Pi", Toast.LENGTH_SHORT).show()
                    }
                    if(readMessage.contains("Removed")){
                        Toast.makeText(requireContext(), "All ports successfully removed", Toast.LENGTH_SHORT).show()
                    }
                    if (readMessage.contains("unknown")) {
                        jsonSend(false)
                    }
                    else{
                        if (jsonSent) {
                            handleJson(readMessage)
                        }
                        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("SSHKeyPref", MODE_PRIVATE)
                        val output: String? = sharedPreferences.getString("public_key", "")
                        Log.d("public_key", output)
                    }
                }

//                Log.d("Tor reply", "" + readMessage)
//                if (isMessageJSON(readMessage)) {
//
//                    val jsonMessage = JSONObject(readMessage)
//re
//                    if (jsonMessage.has("public_key")) {
//                        val myEdit = sharedPreferences.edit()
//                        myEdit.putString("public_key", jsonMessage.getString("public_key"))
//                        myEdit.apply()
//                        bind!!.publicKey.text = jsonMessage.getString("public_key")
//                    }
//                }
//                Constants.MESSAGE_DEVICE_NAME -> handlerCaseName(msg, activity)
//                Constants.MESSAGE_TOAST -> handlerCaseToast(msg)
            }
        }
    }

    private fun buildJSON() {
        try {
            val jsonObject = JSONObject(jsonString)
//            commands = Gson().fromJson(jsonObject.toString(), CommandsList::class.java)
//            updateArrayAdapters(commands)
            Log.d("JSON Object", jsonObject.toString())
            bind!!.publicKey.text = jsonObject.toString()

            val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("SSHKeyPref", MODE_PRIVATE)
            val myEdit = sharedPreferences.edit()
            myEdit.putString("public_key", jsonObject.getString("public_key"))
            myEdit.putString("private_key", jsonObject.getString("private_key"))
            myEdit.apply()

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun handleJson(readMessage: String) {
        Log.d("readMessage", readMessage)
        val s = match(readMessage)
        if (jsonReceiving) {
            jsonString += readMessage
            if (s == RESULTS.END_JSON || s == RESULTS.END_HELP) {
                buildJSON()
                jsonSend(false)
            }
//            else if (s == RESULTS.END_HELP) {
//                showHelpDialog(jsonString)
//                jsonSend(false)
//            }
        } else if (s == RESULTS.START_JSON) {
            jsonReceiving = true
            jsonString = readMessage.trim()
        }
    }
    private fun jsonSend(sent: Boolean) {
        jsonSent = sent
        if (sent) {
            bind!!.progressBar.visibility = View.VISIBLE
        } else {
            bind!!.progressBar.visibility = View.GONE
            jsonReceiving = false
        }
    }
    private fun showHelpDialog(jsonString: String) {
        val b = Bundle()
        b.putString(Constants.JSON_STRING, jsonString)
        val dialogFrag: DialogFragment = HelpDialog()
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT)
        dialogFrag.arguments = b
        dialogFrag.show(requireActivity().supportFragmentManager.beginTransaction(), "helpDialog")
    }
}
