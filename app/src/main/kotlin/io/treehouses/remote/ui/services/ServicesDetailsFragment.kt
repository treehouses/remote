package io.treehouses.remote.ui.services

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.textfield.TextInputEditText
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
import io.treehouses.remote.pojo.enum.Status
import io.treehouses.remote.utils.isLocalUrl
import io.treehouses.remote.utils.isTorURL
import java.util.*

class ServicesDetailsFragment() : BaseServicesFragment(), OnItemSelectedListener, OnPageChangeListener, ServiceAction {
    private var received = false
//    private var wait = false
    private var spinnerAdapter: ServicesListAdapter? = null
//    private var selected: ServiceInfo? = null
    private var serviceCardAdapter: ServiceCardAdapter? = null
    private var scrolled = false
    private var editEnv = false

    private lateinit var binding: ActivityServicesDetailsBinding
    private val viewModel by viewModels<ServicesViewModel>(ownerProducer = {requireParentFragment()})

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        mChatService = listener.getChatService()
        binding = ActivityServicesDetailsBinding.inflate(inflater, container, false)

        viewModel.servicesData.observe(viewLifecycleOwner, Observer {
            if (it.status == Status.SUCCESS) {
                populateServices(viewModel.formattedServices)
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Tutorials.servicesDetailsTutorials(binding, requireActivity())

        viewModel.selectedService.observe(viewLifecycleOwner, Observer {
            goToSelected()
        })

        viewModel.serviceAction.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> {
                    setScreenState(false)
                    return@Observer
                }
                Status.SUCCESS -> {
                    serviceCardAdapter?.notifyDataSetChanged()
                    spinnerAdapter?.notifyDataSetChanged()
                    goToSelected()
                }
                else -> Log.e("UNKNOWN", "RECEIVED in ServiceAction")
            }
            setScreenState(true)
        })
        viewModel.autoRunAction.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> {
                    setScreenState(false)
                    return@Observer
                }
                Status.SUCCESS -> Toast.makeText(context, "Switched autorun to $it", Toast.LENGTH_SHORT).show()
                else -> Log.e("UNKNOWN", "RECEIVED in AutoRun boolean")
            }
            setScreenState(true)
        })
    }

    private fun populateServices(services: MutableList<ServiceInfo>) {
        spinnerAdapter = ServicesListAdapter(requireContext(), services, resources.getColor(R.color.md_grey_600))
        binding.pickService.adapter = spinnerAdapter
        binding.pickService.setSelection(1)
        binding.pickService.onItemSelectedListener = this
        serviceCardAdapter = ServiceCardAdapter(childFragmentManager, services)
        binding.servicesCards.adapter = serviceCardAdapter
        binding.servicesCards.addOnPageChangeListener(this)
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
            if (output.contains("service autorun set")) {
                Toast.makeText(context, "Switched autorun", Toast.LENGTH_SHORT).show()
            }
        }
    }

//    private fun onServiceStatusChanged() {
////        viewModel.selectedService.value = binding.pickService.selectedItem as ServiceInfo
////        Log.d("Entered", "matchOutput: $s")
////        if (s.contains("started")) {
////            viewModel.selectedService.value?.serviceStatus = ServiceInfo.SERVICE_RUNNING
////        } else if (s.contains("stopped and removed")) {
////            viewModel.selectedService.value?.serviceStatus = ServiceInfo.SERVICE_AVAILABLE
////        } else if (s.contains("stopped") || s.contains("installed")) {
////            viewModel.selectedService.value?.serviceStatus = ServiceInfo.SERVICE_INSTALLED
////        } else {
////            return
////        }
////        viewModel.formattedServices.sort()
//    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (!scrolled) {
            if (viewModel.formattedServices[position].isHeader) return
            val count = countHeadersBefore(position)
            binding.servicesCards.currentItem = position - count
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun goToSelected() {
        if (viewModel.selectedService.value == null) return
        val pos = indexOfService(viewModel.selectedService.value!!.name, viewModel.formattedServices)
        if (binding.pickService.selectedItemPosition != pos) {
            binding.pickService.setSelection(pos)
        }
        val count = countHeadersBefore(pos)
        if (binding.servicesCards.currentItem != pos - count) {
            binding.servicesCards.currentItem = pos - count
        }
    }

    override fun onResume() {
        super.onResume()
        goToSelected()
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.selectedService.value != binding.pickService.selectedItem as ServiceInfo) {
            viewModel.selectedService.value = binding.pickService.selectedItem as ServiceInfo
        }

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
            if (viewModel.formattedServices[i].isHeader) count++
        }
        return count
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
                    var command = "treehouses services $name config edit send"
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
        if (s == null) return
        viewModel.onStartClicked(s)
    }

    override fun onClickInstall(s: ServiceInfo?) {
        when {
            s == null -> return
            s.serviceStatus == ServiceInfo.SERVICE_AVAILABLE -> viewModel.onInstallClicked(s)
            s.isOneOf(ServiceInfo.SERVICE_INSTALLED, ServiceInfo.SERVICE_RUNNING) -> {
                val dialog = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle))
                        .setTitle("Delete " + viewModel.selectedService.value?.name + "?")
                        .setMessage("Are you sure you would like to delete this service? All of its data will be lost and the service must be reinstalled.")
                        .setPositiveButton("Delete") { _: DialogInterface?, _: Int ->
                            viewModel.onInstallClicked(s)
                        }.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
                dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()
            }
        }
        viewModel.onInstallClicked(s!!)
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
        if (s == null) return
//        setScreenState(false)
//        fun sendMessage(a1:Int, a2:String, a3:String){
//            listener.sendMessage(getString(a1, a2, a3))
//        }
        viewModel.switchAutoRun(s, newAutoRun)
//        if (newAutoRun) sendMessage(R.string.TREEHOUSES_SERVICES_AUTORUN, s!!.name, "true")
//        else sendMessage(R.string.TREEHOUSES_SERVICES_AUTORUN, s!!.name, "false")
        Toast.makeText(context, "Switching autorun status to $newAutoRun", Toast.LENGTH_SHORT).show()
    }

}