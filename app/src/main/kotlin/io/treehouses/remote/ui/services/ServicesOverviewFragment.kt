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
        bind.listView.adapter = adapter
        bind.listView.onItemClickListener = this

        viewModel.servicesData.observe(viewLifecycleOwner, Observer {
            if (it.status != Status.SUCCESS) return@Observer
            bind.listView.adapter = adapter
            adapter?.notifyDataSetChanged()
        })

        bind.searchBar.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter?.filter?.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        return bind.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Tutorials.servicesOverviewTutorials(bind, requireActivity())
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val selected = viewModel.formattedServices[position]
        viewModel.clickedService.value = selected
    }

    companion object {
        private const val TAG = "ServicesTabFragment"
    }

}