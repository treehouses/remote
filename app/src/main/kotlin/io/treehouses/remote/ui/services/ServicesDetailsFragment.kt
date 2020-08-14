package io.treehouses.remote.ui.services

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
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.adapter.ServiceCardAdapter
import io.treehouses.remote.adapter.ServicesListAdapter
import io.treehouses.remote.callback.ServiceAction
import io.treehouses.remote.databinding.ActivityServicesDetailsBinding
import io.treehouses.remote.databinding.DialogChooseUrlBinding
import io.treehouses.remote.databinding.EnvVarBinding
import io.treehouses.remote.databinding.EnvVarItemBinding
import io.treehouses.remote.pojo.ServiceInfo
import java.util.*

class ServicesDetailsFragment() : BaseServicesFragment(), OnItemSelectedListener, OnPageChangeListener, ServiceAction {
    private var received = false
    private var wait = false
    private var spinnerAdapter: ServicesListAdapter? = null
    private var selected: ServiceInfo? = null
    private var serviceCardAdapter: ServiceCardAdapter? = null
    private var scrolled = false
    private var editEnv = false
    private lateinit var binding: ActivityServicesDetailsBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getViewModel()
        mChatService = listener.getChatService()
        binding = ActivityServicesDetailsBinding.inflate(inflater, container, false)
        viewModel.servicesData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {services ->
            spinnerAdapter = ServicesListAdapter(requireContext(), services, resources.getColor(R.color.md_grey_600))
            binding.pickService.adapter = spinnerAdapter
            binding.pickService.setSelection(1)
            binding.pickService.onItemSelectedListener = this
            serviceCardAdapter = ServiceCardAdapter(childFragmentManager, services)
            binding.servicesCards.adapter = serviceCardAdapter
            binding.servicesCards.addOnPageChangeListener(this)
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Tutorials.servicesDetailsTutorials(binding, requireActivity())
    }

    @JvmField
    val handlerDetails: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    val output = msg.obj as String
                    if (wait) {
                        matchOutput(output.trim { it <= ' ' })
                    }
                    else{
                        handleMore(output)
                    }
                }
                Constants.MESSAGE_STATE_CHANGE -> {
                    listener.redirectHome()
                }
            }
        }
    }

    private fun handleMore(output:String){
        if (isLocalUrl(output, received) || isTorURL(output, received)) {
            received = true
            openLocalURL(output.trim { it <= ' ' })
            binding.progressBar.visibility = View.GONE
        } else if (editEnv) {
            var tokens = output.split(" ")
            val name = tokens[2]
            tokens = tokens.subList(6, tokens.size-1)
            editEnv = false
            showEditDialog(name, tokens.size, tokens)
        } else {
            setScreenState(true)
            if (output.contains("service autorun set")) {
                Toast.makeText(context, "Switched autorun", Toast.LENGTH_SHORT).show()
            } else if (output.toLowerCase(Locale.ROOT).contains("error")) {
                Toast.makeText(context,"An Error occurred", Toast.LENGTH_SHORT).show()
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
        viewModel.servicesData.value?.sort()
        viewModel.servicesData.value = viewModel.servicesData.value
        serviceCardAdapter!!.notifyDataSetChanged()
        spinnerAdapter!!.notifyDataSetChanged()
        setScreenState(true)
        wait = false
        goToSelected()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (!scrolled) {
            val statusCode = viewModel.servicesData.value!![position].serviceStatus
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
            val pos = inServiceList(selected!!.name, viewModel.servicesData.value!!)
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
            if (viewModel.servicesData.value!![i].isHeader) count++
        }
        return count
    }

    private fun runServiceCommand(action: String, name: String) {
        performService(action, name)
        wait = true
        setScreenState(false)
    }

    private fun onStart(selected: ServiceInfo?) {
        if (selected!!.serviceStatus == ServiceInfo.SERVICE_INSTALLED)
            performService("Starting", selected.name)
        else if (selected.serviceStatus == ServiceInfo.SERVICE_RUNNING)
            performService("Stopping", selected.name)
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
        if (s!!.serviceStatus == ServiceInfo.SERVICE_AVAILABLE)
            runServiceCommand("Installing", s.name)
        else if (installedOrRunning(s)) {
            var dialog = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle))
                    .setTitle("Delete " + selected!!.name + "?")
                    .setMessage("Are you sure you would like to delete this service? All of its data will be lost and the service must be reinstalled.")
                    .setPositiveButton("Delete") { _: DialogInterface?, _: Int ->
                        runServiceCommand("Uninstalling", s.name)
                    }.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
    }

    private fun showEditDialog(name: String, size: Int, vars: List<String>) {
        val inflater = requireActivity().layoutInflater; val dialogBinding = EnvVarBinding.inflate(inflater)
        for (i in 0 until size) {
            val rowBinding = EnvVarItemBinding.inflate(inflater)
            val envName = rowBinding.envName
            val newVal = rowBinding.newVal

            envName.text = vars[i].trim { it <= '\"'} + ":"
            newVal.id = i
            envName.setTextColor(ContextCompat.getColor(requireContext(), R.color.daynight_textColor)); newVal.setTextColor(ContextCompat.getColor(requireContext(), R.color.daynight_textColor))
            dialogBinding.varList.addView(rowBinding.root)
        }
        val alertDialog = createEditDialog(dialogBinding.root, name, size, vars)
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }
    private fun createEditDialog(view: View, name: String, size: Int, vars: List<String>): AlertDialog {
        return AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle))
                .setView(view).setTitle("Edit variables").setIcon(R.drawable.dialog_icon)
                .setPositiveButton("Edit"
                ) { _: DialogInterface?, _: Int ->
                    var command = "treehouses services " + name + " config edit send"
                    for (i in 0 until size) {
                        command += " \"" + view.findViewById<TextInputEditText>(i).text + "\""
                    }
                    writeToRPI(command)
                    Toast.makeText(context, "Environment variables changed", Toast.LENGTH_LONG).show()
                }
                .setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .create()
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

    override fun onClickEditEnvVar(s: ServiceInfo?) {
        editEnv = true
        writeToRPI("treehouses services " + s!!.name + " config edit request")
    }

    override fun onClickAutorun(s: ServiceInfo?, newAutoRun: Boolean) {
        setScreenState(false)
        fun sendMessage(a1:Int, a2:String, a3:String){
            listener.sendMessage(getString(a1, a2, a3))
        }
        if (newAutoRun) sendMessage(R.string.TREEHOUSES_SERVICES_AUTORUN, s!!.name, "true")
        else sendMessage(R.string.TREEHOUSES_SERVICES_AUTORUN, s!!.name, "false")
        Toast.makeText(context, "Switching autorun status to $newAutoRun", Toast.LENGTH_SHORT).show()
    }

}