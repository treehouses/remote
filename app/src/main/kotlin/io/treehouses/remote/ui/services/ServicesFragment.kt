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
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.callback.ServicesListener
import io.treehouses.remote.databinding.ActivityServicesFragmentBinding
import io.treehouses.remote.pojo.ServiceInfo
import io.treehouses.remote.utils.SaveUtils
import java.util.*


class ServicesFragment : BaseServicesFragment(), ServicesListener {
    private var servicesTabFragment: ServicesOverviewFragment? = null
    private var servicesDetailsFragment: ServicesDetailsFragment? = null
    var bind: ActivityServicesFragmentBinding? = null
    var worked = false
    private var currentTab:Int =  0
    private lateinit var cachedServicesData: MutableList<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        bind = ActivityServicesFragmentBinding.inflate(inflater, container, false)

        bind!!.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        bind!!.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                replaceFragment(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        setTabEnabled(false)
        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)
        worked = false
        preferences()
        return bind!!.root
    }

    private fun preferences(){
        cachedServicesData = SaveUtils.getStringList(requireContext(), "servicesArray")
        for (string in cachedServicesData) {
            val a = performAction(string, viewModel.servicesData.value!!)
            if (a == 1) {
                worked = true
                showUI()
            }
        }
        writeToRPI("treehouses remote allservices\n")
    }

    private fun showUI(){
        servicesTabFragment = ServicesOverviewFragment()
        servicesDetailsFragment = ServicesDetailsFragment()
//        val bundle = Bundle()
//        bundle.putSerializable("services", services)
//        servicesTabFragment?.arguments = bundle
//        servicesDetailsFragment?.arguments = bundle
        bind!!.progressBar2.visibility = View.GONE
        replaceFragment(currentTab)
    }


    private fun updateListFromRPI(msg:Message){
        val output:String? = msg.obj as String
        when (performAction(output!!, viewModel.servicesData.value!!)) {
            1 -> {
                cachedServicesData.add(output)
                SaveUtils.saveStringList(requireContext(), cachedServicesData, "servicesArray")
                worked = false
                showUI()
            }
            0 -> {
                bind!!.progressBar2.visibility = View.GONE
                showUpdateCliAlert()
            }
            else -> {
                cachedServicesData.add(output)
            }
        }
    }

    override fun getMessage(msg: Message) {
        when (msg.what) {
            Constants.MESSAGE_READ -> {
                updateListFromRPI(msg)
            }
            Constants.MESSAGE_WRITE -> {
                val writeMsg = String((msg.obj as ByteArray))
                Log.d("WRITE", writeMsg)
            }
        }
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
        val tabStrip = bind!!.tabLayout.getChildAt(0) as LinearLayout
        tabStrip.isEnabled = enabled
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).isClickable = enabled
        }
    }

    private fun replaceFragment(position: Int) {
        if (viewModel.servicesData.value.isNullOrEmpty()) return
        setTabEnabled(true)
        var fragment: Fragment? = null
        when (position) {
            0 -> {
                fragment = servicesTabFragment
                if(!worked) mChatService.updateHandler(servicesTabFragment!!.handlerOverview)
            }
            1 -> {
                fragment = servicesDetailsFragment
                if(!worked) {
                    mChatService.updateHandler(servicesDetailsFragment!!.handlerDetails)
                }
            }
        }
        if (fragment != null) {
            val fragmentManager = childFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.main_content, fragment)
            transaction.addToBackStack(null)
            transaction.commitAllowingStateLoss()
        }
    }

    override fun onClick(s: ServiceInfo?) {
        servicesDetailsFragment!!.setSelected(s!!)
        Objects.requireNonNull(bind!!.tabLayout.getTabAt(1))!!.select()
        currentTab = 1
        replaceFragment(currentTab)
    }


    companion object {
        private const val TAG = "ServicesFragment"
    }
}