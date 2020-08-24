package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityTunnelSshFragmentBinding
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.match
import org.json.JSONException
import org.json.JSONObject


class TunnelSSHFragment : BaseFragment(), View.OnClickListener {
    private var addPortButton: Button? = null
    private var addHostButton: Button? = null
    var bind: ActivityTunnelSshFragmentBinding? = null
    private var dropdown: Spinner? = null
    private var portList: ListView? = null
    private var adapter: ArrayAdapter<String>? = null
    private var portsName: java.util.ArrayList<String>? = null
    private var hostsName: java.util.ArrayList<String>? = null
    private var hostsPosition: java.util.ArrayList<Int>? = null
    private lateinit var dialogHosts:Dialog
    private lateinit var dialogKeys:Dialog
    private lateinit var inputExternalHost: TextInputEditText
    private lateinit var inputInternalHost: TextInputEditText
    private lateinit var inputExternal: TextInputEditText
    private lateinit var inputInternal: TextInputEditText
    private lateinit var dialog:Dialog
    private lateinit var addingPortButton: Button
    private lateinit var addingHostButton: Button
    private lateinit var saveKeys: Button
    private lateinit var showKeys: Button
    private lateinit var publicKey: TextView
    private lateinit var privateKey: TextView
    private lateinit var progressBar: ProgressBar
    private var jsonReceiving = false
    private var jsonSent = false
    private var jsonString = ""
    private lateinit var adapter2: ArrayAdapter<String>
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityTunnelSshFragmentBinding.inflate(inflater, container, false)
        bind!!.switchNotification.isEnabled = false
        bind!!.notifyNow.isEnabled = false
        portList = bind!!.sshPorts
        initializeDialog1()
        addPortButton = bind!!.btnAddPort

        addHostButton = bind!!.btnAddHosts
        arrayOf("1", "2", "three")
        hostsName = ArrayList()
        val adapter: ArrayAdapter<String> = ArrayAdapter(this.requireContext(), R.layout.support_simple_spinner_dropdown_item, hostsName!!)
        dropdown?.adapter = adapter
        dropdown?.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3:
            Long) {
                Log.d("winwin", "YYYYY ")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {Log.d("nothing", "YYYYY ")}
        }
        addListeners()
        return bind!!.root
    }

    private fun addListeners() {
        bind!!.switchNotification.setOnCheckedChangeListener { _, isChecked -> switchButton(isChecked) }
        addPortButton!!.setOnClickListener(this)
        addHostButton!!.setOnClickListener(this)
        addingPortButton.setOnClickListener(this)
        addingHostButton.setOnClickListener(this)
        bind!!.notifyNow.setOnClickListener(this)
        bind!!.btnKeys.setOnClickListener(this)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeDialog1() {
        dialog = Dialog(requireContext())
        dialogHosts = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_sshtunnel_ports)
        dropdown = dialog.findViewById(R.id.hosts)
        dialogHosts.setContentView(R.layout.dialog_sshtunnel_hosts)
        inputExternal = dialog.findViewById(R.id.ExternalTextInput)
        inputInternal = dialog.findViewById(R.id.InternalTextInput)
        inputExternalHost = dialogHosts.findViewById(R.id.ExternalTextInput)
        inputInternalHost = dialogHosts.findViewById(R.id.InternalTextInput)
        addingPortButton = dialog.findViewById<Button>(R.id.btn_adding_port)
        addingHostButton = dialogHosts.findViewById<Button>(R.id.btn_adding_host)
        portsName = ArrayList()
        hostsName = ArrayList()
        hostsPosition = ArrayList()
        val window = dialog.window
        val windowHost = dialogHosts.window
        window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        windowHost!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        windowHost.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        try{ initializeDialog2()} catch (exception:Exception){Log.e("Error1", exception.toString())}
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeDialog2(){
        portList!!.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
            if(portsName!![position].contains("@")){
                builder.setTitle("Delete Host  " + portsName!![position] + " ?")
                builder.setPositiveButton("Confirm") { dialog, _ ->
                    val parts = portsName!![position].split(":")[0]
                    listener.sendMessage("treehouses sshtunnel remove host $parts")
                    addHostButton!!.text = "deleting host ....."
                    portList!!.isEnabled = false
                    addHostButton!!.isEnabled = false
                    dialog.dismiss()
                }
            }
            else{
                initializeDialog3(builder, position)
            }
            builder.setNegativeButton("Cancel", null)
            val dialog = builder.create()
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            Log.d("dialoging", "dialog")
            dialog.show()
        }
        initializeDialog4()
    }

    private fun initializeDialog3(builder:AlertDialog.Builder, position:Int){
        builder.setTitle("Delete Port " + portsName!![position] + " ?")
        builder.setPositiveButton("Confirm") { dialog, _ ->
            var myPos:Int = 0
            for(pos in hostsPosition!!.indices){
                if(hostsPosition!![pos] > position){
                    myPos = pos
                    break
                }
            }
            if(hostsPosition!!.last() < position){
                myPos = hostsPosition!!.lastIndex
            }
            Log.d("dasda", myPos.toString())
            listener.sendMessage("treehouses sshtunnel remove port " + portsName!![position].split(":".toRegex(), 2).toTypedArray()[0] + " " + hostsName!![myPos].split(":")[0])
            addPortButton!!.text = "deleting port ....."
            portList!!.isEnabled = false
            addPortButton!!.isEnabled = false
            dialog.dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeDialog4(){
        dialogKeys = Dialog(requireContext())
        dialogKeys.setContentView(R.layout.dialog_sshtunnel_key)
        showKeys = dialogKeys.findViewById(R.id.btn_show_keys)
        saveKeys = dialogKeys.findViewById(R.id.btn_save_keys)
        val profileText = dialogKeys.findViewById<EditText>(R.id.sshtunnel_profile).text

        publicKey = dialogKeys.findViewById(R.id.public_key)
        privateKey = dialogKeys.findViewById(R.id.private_key)
        progressBar = dialogKeys.findViewById(R.id.progress_bar)

        Log.d("profile string", profileText.toString())
        saveKeys.setOnClickListener {
            var profile = profileText.toString()
            listener.sendMessage("treehouses remote key send $profile")
            jsonSend(true)
        }

        showKeys.setOnClickListener {
            var profile = profileText.toString()
            if(profile.isBlank())
                profile = "default"

            val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
            var storedPublicKey: String? = sharedPreferences.getString("${profile}_public_key", "")
            var storedPrivateKey: String? = sharedPreferences.getString("${profile}_private_key", "")

            if (storedPublicKey != null && storedPrivateKey != null) {
                if(storedPublicKey.isBlank()){
                    storedPublicKey = "No public key found"
                }
                if(storedPrivateKey.isBlank()){
                    storedPrivateKey = "No private key found"
                }
            }

            val strPhonePublicKey = Html.fromHtml("<b>Phone Public Key for ${profile}:</b> <br>$storedPublicKey\n", Html.FROM_HTML_MODE_LEGACY)
            val strPhonePrivateKey = Html.fromHtml("<b>Phone Private Key for ${profile}:</b> <br>$storedPrivateKey", Html.FROM_HTML_MODE_LEGACY)
            publicKey.text = strPhonePublicKey
            privateKey.text = strPhonePrivateKey
        }
    }



    private fun getPublicKeys(jsonObject: JSONObject): Pair<String, String> {
        val piPublicKey = jsonObject.getString("public_key")
        val piPrivateKey = jsonObject.getString("private_key")
        return Pair(piPublicKey, piPrivateKey)
    }

    private fun getStoredKeys(profile: String): Pair<String?, String?> {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
        val storedPublicKey: String? = sharedPreferences.getString("${profile}_public_key", "")
        val storedPrivateKey: String? = sharedPreferences.getString("${profile}_private_key", "")
        return Pair(storedPublicKey, storedPrivateKey)
    }

    private fun buildJSON() {
        try {
            val jsonObject = JSONObject(jsonString)

            val profile = jsonObject.getString("profile")

            val (piPublicKey, piPrivateKey) = getPublicKeys(jsonObject)

            val (storedPublicKey, storedPrivateKey) = getStoredKeys(profile)

            Log.d("profile", profile)
            logKeys(piPublicKey, piPrivateKey, storedPublicKey, storedPrivateKey)

            val inPiAndPhone = piPublicKey == storedPublicKey && piPrivateKey == storedPrivateKey
            val inPiOnly = piPublicKey != "No public key found" && piPrivateKey != "No private key found " && storedPublicKey.isNullOrBlank() && storedPrivateKey.isNullOrBlank()
            val inPhoneOnly = piPublicKey == "No public key found" && piPrivateKey == "No private key found " && !storedPublicKey.isNullOrBlank() && !storedPrivateKey.isNullOrBlank()
            val inNeither = piPublicKey == "No public key found" && piPrivateKey == "No private key found " && storedPublicKey.isNullOrBlank() && storedPrivateKey.isNullOrBlank()

            // Pi and phone keys are the same
            if(inPiAndPhone) Toast.makeText(context, "The same keys for $profile are already saved in both Pi and phone", Toast.LENGTH_SHORT).show()
            // Key exists in Pi but not phone
            else if(inPiOnly) handlePhoneKeySave(profile, piPublicKey, piPrivateKey)
            // Key exists in phone but not Pi
            else if(inPhoneOnly) handlePiKeySave(profile, storedPublicKey, storedPrivateKey)
            // Keys don't exist in phone or Pi
            else if(inNeither) Toast.makeText(context, "No keys for $profile exist on either Pi or phone!", Toast.LENGTH_SHORT).show()
            // Keys are different, overwrite one or cancel
            else handleDifferentKeys(jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun handleDifferentKeys(jsonObject: JSONObject) {
        val profile = jsonObject.getString("profile")
        val (piPublicKey, piPrivateKey) = getPublicKeys(jsonObject)
        val (storedPublicKey, storedPrivateKey) = getStoredKeys(profile)

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Overwrite On Pi or Phone")

        val strPiPublicKey = "Pi Public Key for ${profile}: \n$piPublicKey"
        val strPiPrivateKey = "Pi Private Key for ${profile}: \n$piPrivateKey"
        val strPhonePublicKey = "Phone Public Key for ${profile}: \n$storedPublicKey"
        val strPhonePrivateKey = "Phone Private Key for ${profile}: \n$storedPrivateKey"

        val message = ("There are different keys on the Pi and the phone. Would you like to overwrite the Pi's key or the phone's key?\n\n" +
                strPiPublicKey + "\n\n" +
                strPiPrivateKey + "\n\n" +
                strPhonePublicKey + "\n\n" +
                strPhonePrivateKey)

        builder.setMessage(message)

        saveKeyToPhone(builder, profile, piPublicKey, piPrivateKey)

        builder.setNegativeButton("Save to Pi") { _: DialogInterface?, _: Int ->
            listener.sendMessage("treehouses remote key receive \"$storedPublicKey\" \"$storedPrivateKey\" $profile")
            Toast.makeText(context, "The Pi's key has been overwritten with the phone's key successfully ", Toast.LENGTH_LONG).show()
        }
        setNeutralButton(builder, "Cancel")

        builder.show()
    }

    private fun logKeys(piPublicKey: String, piPrivateKey: String, storedPublicKey: String?, storedPrivateKey: String?) {
        Log.d("piPublicKey", piPublicKey)
        Log.d("piPrivateKey", piPrivateKey)
        Log.d("storedPublicKey", storedPublicKey)
        Log.d("storedPrivateKey", storedPrivateKey)
    }

    private fun handlePiKeySave(profile: String, storedPublicKey: String?, storedPrivateKey: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Save Key To Pi")
        builder.setMessage(
                "Phone Public Key for ${profile}: \n$storedPublicKey\n\n" +
                        "Phone Private Key for ${profile}: \n$storedPrivateKey")
        builder.setPositiveButton("Save to Pi") { _: DialogInterface?, _: Int ->
            listener.sendMessage("treehouses remote key receive \"${storedPublicKey}\" \"${storedPrivateKey}\" $profile")
            Toast.makeText(context, "Key saved to Pi successfully", Toast.LENGTH_LONG).show()
        }.setNegativeButton("Cancel") { dialog: DialogInterface?, _: Int ->
            dialog?.dismiss()
        }
        builder.show()
    }

    private fun handlePhoneKeySave(profile: String, piPublicKey: String, piPrivateKey: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Save Key To Phone")
        builder.setMessage("Pi Public Key for ${profile}: \n$piPublicKey\n" +
                "Pi Private Key for ${profile}: \n$piPrivateKey")

        saveKeyToPhone(builder, profile, piPublicKey, piPrivateKey)
        setNeutralButton(builder, "Cancel")

        builder.show()
    }

    private fun saveKeyToPhone(builder: AlertDialog.Builder, profile: String, piPublicKey: String, piPrivateKey: String){
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
        val myEdit = sharedPreferences.edit()
        builder.setPositiveButton("Save to Phone") { _: DialogInterface?, _: Int ->
            myEdit.putString("${profile}_public_key", piPublicKey)
            myEdit.putString("${profile}_private_key", piPrivateKey)
            myEdit.apply()
            Toast.makeText(context, "Key saved to phone successfully", Toast.LENGTH_LONG).show()
        }
    }



    private fun setNeutralButton(builder: AlertDialog.Builder, text: String){
        builder.setNeutralButton(text){ dialog: DialogInterface?, _: Int ->
            dialog?.dismiss()
        }
    }
    private fun handleJson(readMessage: String) {
        val s = match(readMessage)
        if (jsonReceiving) {
            jsonString += readMessage
            if (s == RESULTS.END_JSON || s == RESULTS.END_HELP) {
                buildJSON()
                jsonSend(false)
            }
        } else if (s == RESULTS.START_JSON) {
            jsonReceiving = true
            jsonString = readMessage.trim()
        }
    }

    private fun jsonSend(sent: Boolean) {
        jsonSent = sent
        if (sent) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
            jsonReceiving = false
        }
    }

    private fun switchButton(isChecked:Boolean) {
        bind!!.switchNotification.isEnabled = false
        if (isChecked)  listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE_ON))
        else listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE_OFF))
    }

    private fun addingHostButton(){
        if (inputExternalHost.text.toString().isNotEmpty() && inputInternalHost.text.toString().isNotEmpty() ) {
            if(inputExternalHost.text.toString().contains("@")) {
                val s1 = inputInternalHost.text.toString()
                val s2 = inputExternalHost.text.toString()
                listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_ADD_HOST, s1, s2))
                addHostButton!!.text = "Adding......"
                addHostButton!!.isEnabled = false
            }
            else{
                Toast.makeText(requireContext(), "Invalid host name", Toast.LENGTH_SHORT).show()
            }
            dialogHosts.dismiss()
        }
    }


    private fun addingPortButton(){
        if (inputExternal.text!!.isNotEmpty() && inputInternal.text!!.isNotEmpty()) {
            val s1 = inputInternal.text.toString()
            val s2 = inputExternal.text.toString()
            val parts = dropdown?.selectedItem.toString().split(":")[0]

            listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_ADD_PORT_ACTUAL, s2, s1, parts))
            addPortButton!!.text = "Adding......"
            addPortButton!!.isEnabled = false
            dialog.dismiss()

        }
    }

    override fun onClick(v: View?) {
        fun showDialog(dialog:Dialog){
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
        when (v?.id) {
            R.id.btn_adding_host -> { addingHostButton() }
            R.id.btn_adding_port -> { addingPortButton() }
            R.id.notify_now -> {
                bind!!.notifyNow.isEnabled = false
                listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE_NOW))
            }

            R.id.btn_add_port -> showDialog(dialog)
            R.id.btn_add_hosts -> showDialog(dialogHosts)
            R.id.btn_keys -> showDialog(dialogKeys)
        }
    }

    override fun setUserVisibleHint(visible: Boolean) {
        if(visible) {
            mChatService = listener.getChatService()
            mChatService.updateHandler(mHandler)

            listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE))
            bind!!.sshPorts
            portsName = ArrayList()
//            listener.sendMessage("treehouses sshtunnel notice")
            adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item, portsName!!)
            Log.i("Tag", "Reload fragment")
        }
    }

    private fun handleHostNotFound() {
        addHostButton?.isEnabled = true
        addHostButton?.text = "Add Host"

        addPortButton?.text = "Add Port"
        portList?.isEnabled = true
        addHostButton?.isEnabled = true
        Toast.makeText(requireContext(), "incorrect deleting host/port, try again", Toast.LENGTH_SHORT).show()
    }

    private fun handleModifiedList() {
        Toast.makeText(requireContext(), "Added/Removed. Retrieving port list.", Toast.LENGTH_SHORT).show()

        addPortButton?.text = "Retrieving"
        addHostButton?.text = "Retrieving"
        portsName = ArrayList()
        hostsName = ArrayList()

        hostsPosition = ArrayList()
        listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
    }

    override fun getMessage(msg: Message) {
            if (msg.what == Constants.MESSAGE_READ) {
                val readMessage: String = msg.obj as String
                Log.d("SSHTunnel reply", "" + readMessage)
                val modifyKeywords = arrayOf("ssh-rsa", "Added", "Removed")
                if (readMessage.contains("Host / port not found")) handleHostNotFound()
                else if((modifyKeywords.filter { it in readMessage }).isNotEmpty()) handleModifiedList()
                else if (readMessage.contains("@")) handleNewList(readMessage)
                else if(readMessage.contains("the command 'treehouses sshtunnel ports' returns nothing")) handleNoPorts()
                else if(readMessage.contains("Status: on")) handleOnStatus()
                else getOtherMessages(readMessage)
            }
    }

    private fun handleNewList(readMessage: String) {
        var position = 0
        addPortButton?.isEnabled = true
        addPortButton?.text = "Add Port"; addHostButton?.text = "Add Host"
        addPortButton!!.isEnabled = true; addHostButton?.isEnabled = true
        val hosts = readMessage.split('\n')
        for (host in hosts) {
            val ports = host.split(' ')
            for (port in ports) {
                if (port.length >= 3)
                    portsName!!.add(port)
                if (port.contains("@")) {
                    hostsPosition!!.add(position)
                    hostsName!!.add(port)
                }
                position += 1
            }
        }
        adapter2 = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, hostsName!!)
        dropdown?.adapter = adapter2
        adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item, portsName!!)
        bind!!.sshPorts.adapter = adapter
        portList!!.isEnabled = true
    }

    private fun handleOnStatus() {
        bind!!.switchNotification.isChecked = true;
        bind!!.switchNotification.isEnabled = true;
        bind!!.notifyNow.isEnabled = true
        portsName = ArrayList()
        hostsName = ArrayList()
        hostsPosition = ArrayList()
        listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
    }

    private fun handleNoPorts() {
        adapter2 = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, hostsName!!)
        dropdown?.adapter = adapter2
        adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item, portsName!!)
        bind!!.sshPorts.adapter = adapter
        portList!!.isEnabled = true
        addPortButton!!.text = "Add Port"
        addHostButton!!.text = "Add Host"
        addPortButton!!.isEnabled = false
        addHostButton!!.isEnabled = true
        Toast.makeText(requireContext(), "Add a host", Toast.LENGTH_SHORT).show()
    }

    private fun getOtherMessages(readMessage: String) {
        when {
            readMessage.contains("Status: on") -> {
                bind?.apply {
                    switchNotification.isChecked = true; switchNotification.isEnabled = true
                    notifyNow.isEnabled = true
                }
            }
            readMessage.contains("Status: off") -> {
                bind?.apply {
                    switchNotification.isChecked = false; switchNotification.isEnabled = true
                    notifyNow.isEnabled = true
                    listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
                }
            }
            readMessage.contains("OK.") -> { listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE)) }
            readMessage.contains("Thanks for the feedback!") -> {
                Toast.makeText(requireContext(), "Notified Gitter. Thank you!", Toast.LENGTH_SHORT).show()
                bind!!.notifyNow.isEnabled = true }
            readMessage.contains("Error: only 'list'") -> {
                listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE))
                Toast.makeText(requireContext(), "Please swipe slower in the future as you have a slow rpi, getting ports again...", Toast.LENGTH_SHORT).show()
            }
            readMessage.contains("true") || readMessage.contains("false") ->{
                listener.sendMessage("treehouses remote key send")
                Toast.makeText(context, "Please wait...", Toast.LENGTH_SHORT).show()
            }
            readMessage.contains("Saved") ->{
                Toast.makeText(context, "Keys successfully saved to Pi", Toast.LENGTH_SHORT).show()
            }
            readMessage.contains("unknown")-> {
                jsonSend(false)
            }
            else->{
                if (jsonSent) {
                    handleJson(readMessage)
                }
            }
        }
    }
}

