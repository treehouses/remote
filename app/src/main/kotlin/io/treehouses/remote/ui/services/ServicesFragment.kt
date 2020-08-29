package io.treehouses.remote.ui.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityServicesFragmentBinding
import io.treehouses.remote.pojo.enum.Status
import java.util.*


class ServicesFragment : BaseFragment() {
    private var servicesTabFragment: ServicesOverviewFragment? = null
    private var servicesDetailsFragment: ServicesDetailsFragment? = null
    lateinit var bind: ActivityServicesFragmentBinding
    var worked = false
    private var currentTab:Int =  0

    private val viewModel by viewModels<ServicesViewModel>(ownerProducer = {this})

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityServicesFragmentBinding.inflate(inflater, container, false)
        bind.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        bind.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                replaceFragment(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        setTabEnabled(false)

        worked = false
        showUI()
        viewModel.fetchServicesFromCache()
        viewModel.fetchServicesFromServer()
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.serverServiceData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            when (it.status) {
                Status.LOADING -> bind.progressBar2.visibility = View.VISIBLE
                Status.ERROR -> {
                    bind.progressBar2.visibility = View.GONE
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                }
                else -> bind.progressBar2.visibility = View.GONE
            }
        })

        viewModel.clickedService.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            viewModel.selectedService.value = it
            Objects.requireNonNull(bind.tabLayout.getTabAt(1))?.select()
            currentTab = 1
            replaceFragment(currentTab)
        })
    }

    private fun showUI(){
        servicesTabFragment = ServicesOverviewFragment()
        servicesDetailsFragment = ServicesDetailsFragment()
        replaceFragment(currentTab)
    }

    private fun setTabEnabled(enabled: Boolean) {
        val tabStrip = bind.tabLayout.getChildAt(0) as LinearLayout
        tabStrip.isEnabled = enabled
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).isClickable = enabled
        }
    }

    private fun replaceFragment(position: Int) {
        setTabEnabled(true)
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = servicesTabFragment
            1 -> fragment = servicesDetailsFragment
        }
        if (fragment != null) {
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.main_content, fragment)
            transaction.addToBackStack(null)
            transaction.commitAllowingStateLoss()
        }
    }


    companion object {
        private const val TAG = "ServicesFragment"
    }
}