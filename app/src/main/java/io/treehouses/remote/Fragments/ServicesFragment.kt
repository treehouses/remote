package io.treehouses.remote.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
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
    private var services: ArrayList<ServiceInfo>? = null

    var bind: ActivityServicesFragmentBinding? = null
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
        writeToRPI("treehouses remote allservices\n")
        return bind!!.root
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    val output = msg.obj as String
                    val a = performAction(output, services!!)
                    if (a == 1) {
                        servicesTabFragment = ServicesTabFragment(services)
                        servicesDetailsFragment = ServicesDetailsFragment(services!!)
                        bind!!.progressBar2.visibility = View.GONE
                        replaceFragment(0)
                    } else if (a == 0) {
                        bind!!.progressBar2.visibility = View.GONE
                        val alertDialog = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
                                .setTitle("Please update CLI")
                                .setMessage("Please update to the latest CLI version to access services.")
                                .create()
                        alertDialog.show()
                    }
                }
                Constants.MESSAGE_WRITE -> {
                    val write_msg = String((msg.obj as ByteArray))
                    Log.d("WRITE", write_msg)
                }
            }
        }
    }

    private fun setTabEnabled(enabled: Boolean) {
        val tabStrip = bind!!.tabLayout.getChildAt(0) as LinearLayout
        tabStrip.isEnabled = enabled
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).isClickable = enabled
        }
    }

    private fun replaceFragment(position: Int) {
        if (services!!.isEmpty()) return
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
            transaction.commit()
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