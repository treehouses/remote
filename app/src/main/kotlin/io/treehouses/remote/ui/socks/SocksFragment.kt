package io.treehouses.remote.ui.socks

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.treehouses.remote.R
import io.treehouses.remote.databinding.ActivitySocksFragmentBinding
import io.treehouses.remote.databinding.DialogAddProfileBinding
import io.treehouses.remote.utils.DialogUtils

open class SocksFragment : Fragment() {
    protected val viewModel: SocksViewModel by viewModels(ownerProducer = { this })
    private var textStatus: TextView? = null
    private var adapter: ArrayAdapter<String>? = null
    private lateinit var dialog: Dialog
    lateinit var bind: ActivitySocksFragmentBinding
    private lateinit var bindProfile: DialogAddProfileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = ActivitySocksFragmentBinding.inflate(inflater, container, false)
        bindProfile = DialogAddProfileBinding.inflate(inflater, container, false)
        initializeDialog()
        messageObservers()
        addProfileButtonListeners(dialog)
        return bind.root
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
        dialog.setContentView(bindProfile.root)
        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        initializeObservers()
    }


    private fun addPortListListener() {
        bind.profiles.onItemClickListener = AdapterView.OnItemClickListener { av: AdapterView<*>?, _: View?, position: Int, _: Long ->
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
        viewModel.addProfileButtonText.observe(viewLifecycleOwner) {
            bindProfile.addingProfileButton.text = it
        }
    }

    private fun messageObservers() {
        viewModel.textStatusText.observe(viewLifecycleOwner) {
            textStatus?.text = it
        }

        viewModel.addProfileButtonText.observe(viewLifecycleOwner) {
            bind.btnAddProfile.text = it
        }

        viewModel.addProfileButtonEnabled.observe(viewLifecycleOwner) {
            bind.btnAddProfile.isEnabled = it
        }

        viewModel.refreshList.observe(viewLifecycleOwner) {
            adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item, it)
            bind.profiles.adapter = adapter
        }
    }


    private fun addProfileButtonListeners(dialog: Dialog) {
        bind.btnAddProfile.setOnClickListener {
            dialog.show()
        }
        bindProfile.cancel.setOnClickListener {
            dialog.dismiss()
        }

        bindProfile.addingProfileButton.setOnClickListener {
            val stringMap = mapOf("serverHost" to "${bindProfile.ServerHost.text}",
                "localAddress" to "${bindProfile.LocalAddress.text}", "localPort" to "${bindProfile.localPort.text}",
                "serverPort" to "${bindProfile.serverPort.text}", "password" to "${bindProfile.password.text}"
            )
            viewModel.addProfile(stringMap)
            viewModel.profileDialogDismiss.observe(viewLifecycleOwner) {
                if (it) {
                    dialog.dismiss()
                }
            }
        }
    }
}


