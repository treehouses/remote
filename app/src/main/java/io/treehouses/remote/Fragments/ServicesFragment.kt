package io.treehouses.remote.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
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
import io.treehouses.remote.bases.BaseServicesFragment
import io.treehouses.remote.callback.ServicesListener
import io.treehouses.remote.databinding.ActivityServicesFragmentBinding
import io.treehouses.remote.pojo.ServiceInfo
import java.util.*


class ServicesFragment : BaseServicesFragment(), ServicesListener {
    private var servicesTabFragment: ServicesTabFragment? = null
    private var servicesDetailsFragment: ServicesDetailsFragment? = null
    var bind: ActivityServicesFragmentBinding? = null
    private var counter = 0
    override fun onSaveInstanceState(outState: Bundle) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityServicesFragmentBinding.inflate(inflater, container, false)
        services = ArrayList()
        bind!!.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        bind!!.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                replaceFragment(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        setTabEnabled(false)
        mChatService = listener.getChatService()
        mChatService.updateHandler(handler)
        preferences()
        return bind!!.root
    }

    private fun preferences(){
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("ServicesPref", MODE_PRIVATE)
        var worked = false

        val max: Int = sharedPreferences.getInt("max", 0)
        for (i in 1..max) {
            val output:String? = sharedPreferences.getString("output$i", "")
            val a = performAction(output!!, services)
            if (a == 1) {
                showUI()
                writeToRPI("treehouses remote allservices\n")
                worked = true
            }
        }
        if(!worked){
            writeToRPI("treehouses remote allservices\n")
        }

    }

    private fun showUI(){
        servicesTabFragment = ServicesTabFragment()
        servicesDetailsFragment = ServicesDetailsFragment()
        val bundle = Bundle()
        bundle.putSerializable("services", services)
        servicesTabFragment?.arguments = bundle
        servicesDetailsFragment?.arguments = bundle
        bind!!.progressBar2.visibility = View.GONE
        replaceFragment(0)
    }


    private fun updateListFromRPI(msg:Message){
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("ServicesPref", MODE_PRIVATE)
        val output:String? = msg.obj as String
        when (performAction(output!!, services)) {
            1 -> {
                counter += 1
                val myEdit = sharedPreferences.edit()
                myEdit.putString("output$counter", output)
                myEdit.putInt("max", counter)
                myEdit.apply()
                showUI()
            }
            0 -> {
                bind!!.progressBar2.visibility = View.GONE
                showUpdateCliAlert()
            }
            else -> {
                counter += 1
                val myEdit = sharedPreferences.edit()
                myEdit.putString("output$counter", output)
                myEdit.apply()
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
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
    }

    private fun showUpdateCliAlert() {
        val alertDialog = createDialog(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle),
                "Please update CLI",
                "Please update to the latest CLI version to access services.")
                .create()
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
        if (services.isEmpty()) return
        setTabEnabled(true)
        var fragment: Fragment? = null
        when (position) {
            0 -> {
                fragment = servicesTabFragment
                mChatService.updateHandler(servicesTabFragment!!.handlerOverview)
            }
            1 -> {
                fragment = servicesDetailsFragment
                mChatService.updateHandler(servicesDetailsFragment!!.handlerDetails)
            }
            else -> {
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
        replaceFragment(1)
    }


    companion object {
        private const val TAG = "ServicesFragment"
    }
}