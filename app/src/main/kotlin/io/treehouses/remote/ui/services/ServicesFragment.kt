package io.treehouses.remote.ui.services

import android.app.AlertDialog
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.ContextThemeWrapper
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
import io.treehouses.remote.databinding.ActivityServicesFragmentBinding
import io.treehouses.remote.pojo.enum.Status
import java.util.*


class ServicesFragment : BaseServicesFragment() {
    private var servicesTabFragment: ServicesOverviewFragment? = null
    private var servicesDetailsFragment: ServicesDetailsFragment? = null
    lateinit var bind: ActivityServicesFragmentBinding
    var worked = false
    private var currentTab:Int =  0

//    private lateinit var cachedServices: MutableList<String>
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
        viewModel.fetchServicesFromServer()

        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.servicesData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            when (it.status) {
                Status.SUCCESS -> showUI()
                Status.LOADING -> bind.progressBar2.visibility = View.VISIBLE
                Status.ERROR -> {
                    bind.progressBar2.visibility = View.GONE
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                }
                else -> { }
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
//        viewModel.servicesData.value = Resource.success(services)
        bind.progressBar2.visibility = View.GONE
        replaceFragment(currentTab)
    }


    private fun updateListFromRPI(msg:Message){
        val output:String? = msg.obj as String
//        when (performAction(output!!, services)) {
//            1 -> {
//                cachedServices.add(output)
//                SaveUtils.saveStringList(requireContext(), cachedServices, "servicesArray")
//                worked = false
//                showUI()
//            }
//            0 -> {
//                bind!!.progressBar2.visibility = View.GONE
//                showUpdateCliAlert()
//            }
//            else -> {
//                cachedServices.add(output)
//            }
//        }
    }

    private fun showUpdateCliAlert() {
        val alertDialog = createDialog(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle),
                "Please update CLI",
                "Please update to the latest CLI version to access services.")
                .create()
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    private fun createDialog(con:ContextThemeWrapper,  title:String,  message:String):AlertDialog.Builder {
        return AlertDialog.Builder(con).setTitle(title).setMessage(message)
    }

    private fun setTabEnabled(enabled: Boolean) {
        val tabStrip = bind.tabLayout.getChildAt(0) as LinearLayout
        tabStrip.isEnabled = enabled
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).isClickable = enabled
        }
    }

    private fun replaceFragment(position: Int) {
        if (viewModel.formattedServices.isEmpty()) return
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