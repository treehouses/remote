package io.treehouses.remote.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.adapter.ServiceCardAdapter
import io.treehouses.remote.adapter.ServicesListAdapter
import io.treehouses.remote.bases.BaseServicesFragment
import io.treehouses.remote.callback.ServiceAction
import io.treehouses.remote.databinding.ActivityServicesDetailsBinding
import io.treehouses.remote.databinding.DialogChooseUrlBinding
import io.treehouses.remote.pojo.ServiceInfo
import java.util.*

class ServicesDetailsFragment() : BaseServicesFragment(), OnItemSelectedListener, OnPageChangeListener, ServiceAction {
    private var received = false
    private var wait = false
    private var spinnerAdapter: ServicesListAdapter? = null
    private var selected: ServiceInfo? = null
    private var serviceCardAdapter: ServiceCardAdapter? = null
    private var scrolled = false
    private lateinit var binding: ActivityServicesDetailsBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mChatService = listener.getChatService()
        binding = ActivityServicesDetailsBinding.inflate(inflater, container, false)
        spinnerAdapter = ServicesListAdapter(requireContext(), services, resources.getColor(R.color.md_grey_600))
        binding.pickService.adapter = spinnerAdapter
        binding.pickService.setSelection(1)
        binding.pickService.onItemSelectedListener = this
        serviceCardAdapter = ServiceCardAdapter(childFragmentManager, services)
        binding.servicesCards.adapter = serviceCardAdapter
        binding.servicesCards.addOnPageChangeListener(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Tutorials.servicesDetailsTutorials(binding, requireActivity())
    }

    @JvmField
    val handlerDetails: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    val output = msg.obj as String
                    moreActions(output)
                }
            }
        }
    }

    private fun matchOutput(s: String) {
        selected = binding.pickService.selectedItem as ServiceInfo
        Log.d("Entered", "matchOutput: $s")
        if (s.contains("started")) {
            selected!!.serviceStatus = ServiceInfo.SERVICE_RUNNING
        } else if (s.contains("stopped and removed")) {
            selected!!.serviceStatus = ServiceInfo.SERVICE_AVAILABLE
            Log.d("STOP", "matchOutput: ")
        } else if (s.contains("stopped") || s.contains("installed")) {
            selected!!.serviceStatus = ServiceInfo.SERVICE_INSTALLED
        } else {
            return
        }
        services.sort()
        serviceCardAdapter!!.notifyDataSetChanged()
        spinnerAdapter!!.notifyDataSetChanged()
        setScreenState(true)
        wait = false
        goToSelected()
    }

    private fun moreActions(output: String) {
        if (wait) {
            matchOutput(output.trim { it <= ' ' })
        } else if (isLocalUrl(output, received) || isTorURL(output, received)) {
            received = true
            openLocalURL(output.trim { it <= ' ' })
            binding.progressBar.visibility = View.GONE
        } else {
            setScreenState(true)
            var msg = ""
            if (output.contains("service autorun set")) {
                msg = "Switched autorun"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            } else if (output.toLowerCase(Locale.ROOT).contains("error")) {
                msg = "An Error occurred"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (!scrolled) {
            val statusCode = services[position].serviceStatus
            if (statusCode == ServiceInfo.SERVICE_HEADER_AVAILABLE || statusCode == ServiceInfo.SERVICE_HEADER_INSTALLED) return
            val count = countHeadersBefore(position)
            binding.servicesCards.currentItem = position - count
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    fun setSelected(s: ServiceInfo) {
        Log.d("SELECTED", "setSelected: " + s.name)
        selected = s
    }

    private fun goToSelected() {
        if (selected != null) {
            val pos = inServiceList(selected!!.name, services)
            val count = countHeadersBefore(pos)
            binding.servicesCards.currentItem = pos - count
            binding.pickService.setSelection(pos)
        }
    }

    override fun onResume() {
        super.onResume()
        goToSelected()
    }

    override fun onPause() {
        super.onPause()
        selected = binding.pickService.selectedItem as ServiceInfo
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        Log.d("SELECTED", "onPageSelected: ")
        scrolled = true
        val pos = position + countHeadersBefore(position + 1)
        binding.pickService.setSelection(pos)
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
        var dialog = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle))
                .setTitle("Delete " + selected!!.name + "?")
                .setMessage("Are you sure you would like to delete this service? All of its data will be lost and the service must be reinstalled.")
                .setPositiveButton("Delete") { _: DialogInterface?, _: Int ->
                    performService("Uninstalling", getString(R.string.TREEHOUSES_SERVICES_CLEANUP, selected.name), selected.name)
                    performServiceWait()
                }.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun onInstall(selected: ServiceInfo?) {
        if (selected!!.serviceStatus == ServiceInfo.SERVICE_AVAILABLE) {
            performService("Installing", getString(R.string.TREEHOUSES_SERVICES_INSTALL, selected.name), selected.name)
            performServiceWait()
        } else if (installedOrRunning(selected)) {
            showDeleteDialog(selected)
        }
    }

    private fun performServiceWait() {
        wait = true
        setScreenState(false)
    }

    private fun onStart(selected: ServiceInfo?) {
        if (selected!!.serviceStatus == ServiceInfo.SERVICE_INSTALLED) {
            performService("Starting", getString(R.string.TREEHOUSES_SERVICES_UP, selected.name), selected.name)
        } else if (selected.serviceStatus == ServiceInfo.SERVICE_RUNNING) {
            performService("Stopping", getString(R.string.TREEHOUSES_SERVICES_STOP, selected.name), selected.name)
        }
    }

    private fun setOnClick(v: View, command: String, alertDialog: AlertDialog) {
        v.setOnClickListener {
            writeToRPI(command)
            alertDialog.dismiss()
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private fun onLink(selected: ServiceInfo?) {
        //reqUrls();
        val chooseBind = DialogChooseUrlBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle)).setView(chooseBind.root).setTitle("Select URL type").create()
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setOnClick(chooseBind.localButton, getString(R.string.TREEHOUSES_SERVICES_URL_LOCAL, selected!!.name), alertDialog)
        setOnClick(chooseBind.torButton, getString(R.string.TREEHOUSES_SERVICES_URL_TOR, selected.name), alertDialog)
        alertDialog.show()
    }

    private fun setScreenState(state: Boolean) {
        binding.servicesCards.setPagingEnabled(state)
        binding.pickService.isEnabled = state
        if (state) binding.progressBar.visibility = View.GONE else binding.progressBar.visibility = View.VISIBLE
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
        if (newAutoRun) listener.sendMessage(getString(R.string.TREEHOUSES_SERVICES_AUTORUN, s!!.name, "true"))
        else listener.sendMessage(getString(R.string.TREEHOUSES_SERVICES_AUTORUN, s!!.name, "false"))
        Toast.makeText(context, "Switching autorun status to $newAutoRun", Toast.LENGTH_SHORT).show()
    }

}