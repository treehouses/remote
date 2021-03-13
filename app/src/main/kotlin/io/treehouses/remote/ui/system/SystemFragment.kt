package io.treehouses.remote.ui.system

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.adapter.NetworkListAdapter
import io.treehouses.remote.adapter.ViewHolderTether
import io.treehouses.remote.adapter.ViewHolderVnc
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivitySystemFragmentBinding
import io.treehouses.remote.pojo.NetworkListItem
import io.treehouses.remote.utils.DialogUtils
import io.treehouses.remote.utils.logD
import java.util.*

class SystemFragment : BaseFragment() {

    protected val viewModel: SystemViewModel by viewModels(ownerProducer = { this })
    private lateinit var bind: ActivitySystemFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivitySystemFragmentBinding.inflate(inflater, container, false)
        val adapter = NetworkListAdapter(requireContext(), NetworkListItem.systemList)
        adapter.setListener(listener)
        bind.listView.setOnGroupExpandListener { groupPosition: Int ->
            viewModel.onClickListItem(groupPosition)
        }
        bind.listView.setAdapter(adapter)
        Tutorials.systemTutorials(bind, requireActivity())
        return bind.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.sendMessageAndHostname()
    }
}