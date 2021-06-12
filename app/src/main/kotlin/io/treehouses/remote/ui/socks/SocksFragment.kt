package io.treehouses.remote.ui.socks

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.treehouses.remote.R
import io.treehouses.remote.databinding.ActivitySocksFragmentBinding
import io.treehouses.remote.databinding.DialogAddProfileBinding
import io.treehouses.remote.utils.DialogUtils

class SocksFragment : Fragment() {

    protected val viewModel: SocksViewModel by viewModels(ownerProducer = { this })
    private var addProfileButton: Button? = null
    private var addingProfileButton: Button? = null
    private var cancelProfileButton: Button? = null
    private var textStatus: TextView? = null
    private var adapter: ArrayAdapter<String>? = null
    private lateinit var dialog: Dialog
    private lateinit var password: EditText
    private lateinit var serverPort: EditText
    private lateinit var localPort: EditText
    private lateinit var localAddress: EditText
    private lateinit var serverHost: EditText
    private var portList: ListView? = null
    var bind: ActivitySocksFragmentBinding? = null
    var bindProfile: DialogAddProfileBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivitySocksFragmentBinding.inflate(inflater, container, false)
        bindProfile = DialogAddProfileBinding.inflate(inflater, container, false)
//        viewModel.onLoad()
        addProfileButton = bind!!.btnAddProfile
        portList = bind!!.profiles
        initializeDialog()
        messageObservers()
        addProfileButtonListeners(dialog)
        portList = bind!!.profiles

        return bind!!.root
    }

    override fun setUserVisibleHint(visible: Boolean) {
        if (visible) {
            viewModel.onLoad()
            viewModel.listenerInitialized()
        }
    }


    private fun initializeDialog() {
        dialog = Dialog(requireContext())
        addPortListListener()

        dialog.setContentView(bindProfile!!.root)

        serverHost = bindProfile!!.ServerHost
        serverPort = bindProfile!!.serverPort
        localAddress = bindProfile!!.LocalAddress
        localPort = bindProfile!!.localPort
        password = bindProfile!!.password
        addingProfileButton = bindProfile!!.addingProfileButton
        cancelProfileButton = bindProfile!!.cancel
        val window = dialog.window
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        initializeObservers()
    }


    private fun addPortListListener() {
        portList!!.onItemClickListener = AdapterView.OnItemClickListener { av: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val builder = AlertDialog.Builder(activity, R.style.CustomAlertDialogStyle)
            val selectedString = av?.getItemAtPosition(position)
            builder.setTitle("Delete Profile $selectedString ?")
            DialogUtils.createAlertDialog(context, "Delete Profile $selectedString ?") {
                viewModel.sendMessage("treehouses shadowsocks remove $selectedString ")
                Toast.makeText(activity, "Removing profile...", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }

    private fun initializeObservers() {
        viewModel.addProfileButtonText.observe(viewLifecycleOwner, Observer {
            addingProfileButton?.setText(it)
        })
    }

    private fun messageObservers() {
        viewModel.textStatusText.observe(viewLifecycleOwner, Observer {
            textStatus?.text = it
        })



        viewModel.addProfileButtonText.observe(viewLifecycleOwner, Observer {
            addProfileButton?.text = it
        })

        viewModel.addProfileButtonEnabled.observe(viewLifecycleOwner, Observer {
            addProfileButton?.isEnabled = it
        })

        viewModel.refreshList.observe(viewLifecycleOwner, Observer {
            adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item, it)
            bind!!.profiles.adapter = adapter
        })
    }


    private fun addProfileButtonListeners(dialog: Dialog) {

        addProfileButton!!.setOnClickListener {
            dialog.show()
        }
        cancelProfileButton!!.setOnClickListener {
            dialog.dismiss()
        }
        addingProfileButton!!.setOnClickListener {
            val stringMap = mapOf("serverHost" to serverHost.text.toString(),
                    "localAddress" to localAddress.text.toString(), "localPort" to localPort.text.toString(),
                    "serverPort" to serverPort.text.toString(), "password" to password.text.toString())
            viewModel.addProfile(stringMap)
            viewModel.profileDialogDismiss.observe(viewLifecycleOwner, Observer {
                if (it) dialog.dismiss()
            })
        }
    }

}


