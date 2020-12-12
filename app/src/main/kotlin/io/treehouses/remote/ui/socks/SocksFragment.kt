package io.treehouses.remote.ui.socks

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivitySocksFragmentBinding
import io.treehouses.remote.databinding.DialogAddProfileBinding
import io.treehouses.remote.ui.status.StatusViewModel

class SocksFragment : BaseFragment(){
    protected val viewModel: SocksViewModel by viewModels(ownerProducer = { this })
    private var startButton: Button? = null
    private var addProfileButton: Button? = null
    private var addingProfileButton: Button? = null
    private var cancelProfileButton: Button? = null
    private var textStatus: TextView? = null
    private var adapter: ArrayAdapter<String>? = null
    private var profileName: java.util.ArrayList<String>? = null
    private var portList: ListView? = null
    var bind: ActivitySocksFragmentBinding? = null
    private lateinit var dialog: Dialog
    var bindProfile: DialogAddProfileBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivitySocksFragmentBinding.inflate(inflater, container, false)
        bindProfile = DialogAddProfileBinding.inflate(inflater, container, false)
        profileName = ArrayList()
        addProfileButton = bind!!.btnAddProfile
        portList = bind!!.profiles
        initializeObservers()
        addProfileButtonListeners(dialog)
        portList = bind!!.profiles
        return bind!!.root
    }

    private fun initializeObservers(){
        dialog = Dialog(requireContext())
        addPortListListener()

        dialog.setContentView(bindProfile!!.root)

        //serverHost = bindProfile!!.ServerHost
        viewModel.serverHostText.observe(viewLifecycleOwner, Observer {
            viewModel.serverHostText.value = bindProfile!!.ServerHost
            }
        )
        localAddress = bindProfile!!.LocalAddress
        localPort = bindProfile!!.localPort
        serverPort = bindProfile!!.serverPort
        password = bindProfile!!.password
        addingProfileButton = bindProfile!!.addingProfileButton
        cancelProfileButton = bindProfile!!.cancel
        val window = dialog.window
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        window.setBackgroundDrawableResource(android.R.color.transparent)
    }
}