package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Views.ServiceViewPager
import io.treehouses.remote.adapter.ServiceCardAdapter
import io.treehouses.remote.adapter.ServicesListAdapter
import io.treehouses.remote.bases.BaseServicesFragment
import io.treehouses.remote.callback.ServiceAction
import io.treehouses.remote.pojo.ServiceInfo
import java.util.*

class ServicesDetailsFragment internal constructor(private val services: ArrayList<ServiceInfo>) : BaseServicesFragment(), OnItemSelectedListener, OnPageChangeListener, ServiceAction {
    private var view: View? = null
    private var serviceSelector: Spinner? = null
    private var progressBar: ProgressBar? = null
    private var received = false
    private var wait = false
    private var spinnerAdapter: ServicesListAdapter? = null
    private var selected: ServiceInfo? = null
    private var serviceCards: ServiceViewPager? = null
    private var serviceCardAdapter: ServiceCardAdapter? = null
    private var scrolled = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mChatService = listener.chatService!!
        //        mChatService.updateHandler(mHandler);
        view = inflater.inflate(R.layout.activity_services_details, container, false)
        serviceSelector = view.findViewById(R.id.pickService)
        progressBar = view.findViewById(R.id.progressBar)
        spinnerAdapter = ServicesListAdapter(context!!, services, resources.getColor(R.color.md_grey_600))
        serviceSelector.setAdapter(spinnerAdapter)
        serviceSelector.setSelection(1)
        serviceSelector.setOnItemSelectedListener(this)
        serviceCards = view.findViewById(R.id.services_cards)
        serviceCardAdapter = ServiceCardAdapter(childFragmentManager, services)
        serviceCards.setAdapter(serviceCardAdapter)
        serviceCards.addOnPageChangeListener(this)
        return view
    }

    val handlerDetails: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    val output = msg.obj as String
                    moreActions(output)
                }
                Constants.MESSAGE_WRITE ->
                    var  write_msg
                    : String
                    ?
                    = String((msg.obj as ByteArray))
            }
        }
    }

    private fun matchOutput(s: String) {
        selected = serviceSelector!!.selectedItem as ServiceInfo
        Log.d("Entered", "matchOutput: $s")
        if (s.contains("started")) {
            selected!!.serviceStatus = ServiceInfo.SERVICE_RUNNING
        } else if (s.contains("stopped and removed")) {
            selected!!.serviceStatus = ServiceInfo.SERVICE_AVAILABLE
            Log.d("STOP", "matchOutput: ")
        } else if (s.contains("stopped") && !s.contains("removed")) {
            selected!!.serviceStatus = ServiceInfo.SERVICE_INSTALLED
        } else if (s.contains("installed")) {
            selected!!.serviceStatus = ServiceInfo.SERVICE_INSTALLED
        } else {
            return
        }
        Collections.sort(services)
        serviceCardAdapter!!.notifyDataSetChanged()
        spinnerAdapter!!.notifyDataSetChanged()
        setScreenState(true)
        wait = false
        goToSelected()
    }

    private fun moreActions(output: String) {
        if (wait) {
            matchOutput(output.trim { it <= ' ' })
        } else if (isLocalUrl(output, received)) {
            received = true
            openLocalURL(output.trim { it <= ' ' })
            progressBar!!.visibility = View.GONE
        } else if (isTorURL(output, received)) {
            received = true
            openTorURL(output.trim { it <= ' ' })
            progressBar!!.visibility = View.GONE
        } else if (output.contains("service autorun set")) {
            setScreenState(true)
            Toast.makeText(context, "Switched autorun", Toast.LENGTH_SHORT).show()
        } else if (output.toLowerCase().contains("error")) {
            setScreenState(true)
            Toast.makeText(context, "An Error occurred", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        if (!scrolled) {
            val statusCode = services[position].serviceStatus
            if (statusCode == ServiceInfo.SERVICE_HEADER_AVAILABLE || statusCode == ServiceInfo.SERVICE_HEADER_INSTALLED) return
            val count = countHeadersBefore(position)
            serviceCards!!.currentItem = position - count
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    fun setSelected(s: ServiceInfo) {
        Log.d("SELECTED", "setSelected: " + s.name)
        selected = s
    }

    private fun goToSelected() {
        if (selected != null && serviceSelector != null) {
            val pos: Int = inServiceList(selected!!.name, services)
            val count = countHeadersBefore(pos)
            serviceCards!!.currentItem = pos - count
            serviceSelector!!.setSelection(pos)
        }
    }

    override fun onResume() {
        super.onResume()
        goToSelected()
    }

    override fun onPause() {
        super.onPause()
        selected = serviceSelector!!.selectedItem as ServiceInfo
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        Log.d("SELECTED", "onPageSelected: ")
        scrolled = true
        val pos = position + countHeadersBefore(position + 1)
        serviceSelector!!.setSelection(pos)
        scrolled = false
    }

    override fun onPageScrollStateChanged(state: Int) {}
    private fun countHeadersBefore(position: Int): Int {
        var count = 0
        for (i in 0..position) {
            if (services[i].isHeader) count++
        }
        return count
    }

    private fun showDeleteDialog(selected: ServiceInfo?) {
        AlertDialog.Builder(context)
                .setTitle("Delete " + selected!!.name + "?")
                .setMessage("Are you sure you would like to delete this service? All of its data will be lost and the service must be reinstalled.")
                .setPositiveButton("Delete") { dialog: DialogInterface?, which: Int ->
                    performService("Uninstalling", """treehouses services ${selected.name} cleanup
""", selected.name)
                    wait = true
                    setScreenState(false)
                }.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int -> dialog.dismiss() }.create().show()
    }

    private fun onInstall(selected: ServiceInfo?) {
        if (selected!!.serviceStatus == ServiceInfo.SERVICE_AVAILABLE) {
            performService("Installing", """treehouses services ${selected.name} install
""", selected.name)
            wait = true
            setScreenState(false)
        } else if (installedOrRunning(selected)) {
            showDeleteDialog(selected)
        }
    }

    private fun onStart(selected: ServiceInfo?) {
        if (selected!!.serviceStatus == ServiceInfo.SERVICE_INSTALLED) {
            performService("Starting", """treehouses services ${selected.name} up
""", selected.name)
        } else if (selected.serviceStatus == ServiceInfo.SERVICE_RUNNING) {
            performService("Stopping", """treehouses services ${selected.name} stop
""", selected.name)
        }
    }

    private fun setOnClick(v: View, id: Int, command: String, alertDialog: AlertDialog) {
        v.findViewById<View>(id).setOnClickListener { v1: View? ->
            writeToRPI(command)
            alertDialog.dismiss()
            progressBar!!.visibility = View.VISIBLE
        }
    }

    private fun onLink(selected: ServiceInfo?) {
        //reqUrls();
        val view = layoutInflater.inflate(R.layout.dialog_choose_url, null)
        val alertDialog = AlertDialog.Builder(context).setView(view).setTitle("Select URL type").create()
        setOnClick(view, R.id.local_button, """treehouses services ${selected!!.name} url local
""", alertDialog)
        setOnClick(view, R.id.tor_button, """treehouses services ${selected.name} url tor
""", alertDialog)
        alertDialog.show()
    }

    private fun setScreenState(state: Boolean) {
        serviceCards!!.setPagingEnabled(state)
        serviceSelector!!.isEnabled = state
        if (state) progressBar!!.visibility = View.GONE else progressBar!!.visibility = View.VISIBLE
    }

    override fun onClickInstall(s: ServiceInfo?) {
        onInstall(s)
    }

    override fun onClickStart(s: ServiceInfo?) {
        onStart(s)
        wait = true
        setScreenState(false)
    }

    override fun onClickLink(s: ServiceInfo?) {
        onLink(s)
        received = false
    }

    override fun onClickAutorun(s: ServiceInfo?, newAutoRun: Boolean) {
        setScreenState(false)
        if (newAutoRun) listener.sendMessage("""treehouses services ${s!!.name} autorun true
""") else listener.sendMessage("""treehouses services ${s!!.name} autorun false
""")
        Toast.makeText(context, "Switching autorun status to $newAutoRun", Toast.LENGTH_SHORT).show()
    }

}