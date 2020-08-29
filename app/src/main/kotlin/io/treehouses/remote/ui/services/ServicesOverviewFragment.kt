package io.treehouses.remote.ui.services

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.adapter.ServicesListAdapter
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityServicesTabFragmentBinding
import io.treehouses.remote.pojo.enum.Status

class ServicesOverviewFragment() : BaseFragment(), OnItemClickListener {
    private var adapter: ServicesListAdapter? = null
    private lateinit var bind: ActivityServicesTabFragmentBinding

    private val viewModel by viewModels<ServicesViewModel>(ownerProducer = {requireParentFragment()})

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityServicesTabFragmentBinding.inflate(inflater, container, false)
        adapter = ServicesListAdapter(requireContext(), viewModel.formattedServices, ContextCompat.getColor(requireContext(), R.color.bg_white))

        bind.searchBar.doOnTextChanged { text, _, _, _ ->
            adapter?.filter?.filter(text.toString())
        }
        return bind.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Tutorials.servicesOverviewTutorials(bind, requireActivity())

        bind.listView.adapter = adapter
        bind.listView.onItemClickListener = this
        //Only update the adapter if the result is a success
        viewModel.servicesData.observe(viewLifecycleOwner, Observer {
            if (it.status != Status.SUCCESS) return@Observer
            bind.listView.adapter = adapter
            adapter?.notifyDataSetChanged()
        })
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val selected = viewModel.formattedServices[position]
        viewModel.clickedService.value = selected
    }

    companion object {
        private const val TAG = "ServicesTabFragment"
    }

}