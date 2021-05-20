package io.treehouses.remote.ui.system

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import io.treehouses.remote.Tutorials
import io.treehouses.remote.adapter.SystemListAdapter
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivitySystemFragmentBinding
import io.treehouses.remote.pojo.NetworkListItem
import io.treehouses.remote.utils.logD
import me.toptas.fancyshowcase.FocusShape

class SystemFragment : BaseFragment() {

    protected val viewModel: SystemViewModel by viewModels(ownerProducer = { this })
    private lateinit var bind: ActivitySystemFragmentBinding
    lateinit var adapter: SystemListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivitySystemFragmentBinding.inflate(inflater, container, false)
        adapter = SystemListAdapter(requireContext(), NetworkListItem.systemList)
        adapter.setListener(listener)
        bind.listView.setOnGroupExpandListener { groupPosition: Int ->
            viewModel.onClickListItem(groupPosition)
        }
        bind.listView.setAdapter(adapter)
        //Tutorials.systemTutorials(bind, requireActivity())
        return bind.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler().postDelayed({
            logD("View Size " + adapter.getViews().size)
//            adapter.getViews().forEach {

//
//            }
            Tutorials.systemTutorials(requireActivity(),adapter.getViews());
        }, 3000);
    }

    override fun onResume() {
        super.onResume()
        viewModel.sendMessageAndHostname()
    }
}