package io.treehouses.remote.ui.services

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
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
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.ServiceAction
import io.treehouses.remote.databinding.ActivityServicesDetailsBinding
import io.treehouses.remote.databinding.DialogChooseUrlBinding
import io.treehouses.remote.databinding.EnvVarBinding
import io.treehouses.remote.databinding.EnvVarItemBinding
import io.treehouses.remote.pojo.ServiceInfo
import io.treehouses.remote.pojo.enum.Status
import io.treehouses.remote.utils.indexOfService

class ServicesDetailsFragment() : BaseFragment(), OnItemSelectedListener, ServiceAction {
    private var spinnerAdapter: ServicesListAdapter? = null
    private var serviceCardAdapter: ServiceCardAdapter? = null
    private var scrolled = false

    private lateinit var binding: ActivityServicesDetailsBinding
    private val viewModel by viewModels<ServicesViewModel>(ownerProducer = {requireParentFragment()})

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivityServicesDetailsBinding.inflate(inflater, container, false)
//        spinnerAdapter = ServicesListAdapter(requireContext(), viewModel.formattedServices, resources.getColor(R.color.md_grey_600))
//        serviceCardAdapter = ServiceCardAdapter(childFragmentManager, viewModel.formattedServices)
//        populateServices()

        viewModel.servicesData.observe(viewLifecycleOwner, Observer {
            if (it.status == Status.SUCCESS) {
                spinnerAdapter = ServicesListAdapter(requireContext(), viewModel.formattedServices, resources.getColor(R.color.md_grey_600))
                serviceCardAdapter = ServiceCardAdapter(childFragmentManager, viewModel.formattedServices)
                populateServices()
                goToSelected()
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
            Log.e("SERVICEACTION", it.data.toString())
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

        viewModel.error.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.error.value = ""
            }
            setScreenState(true)
        })

        observeMoreActions()
    }

    private fun observeMoreActions() {
        viewModel.autoRunAction.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> {
                    setScreenState(false)
                    return@Observer
                }
                Status.SUCCESS -> Toast.makeText(context, "Switched autorun to ${it.data}", Toast.LENGTH_SHORT).show()
                else -> Log.e("UNKNOWN", "RECEIVED in AutoRun boolean")
            }
            setScreenState(true)
        })

        viewModel.getLinkAction.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    openURL(it.data.toString())
                    binding.progressBar.visibility = View.GONE
                }
                Status.LOADING -> binding.progressBar.visibility = View.VISIBLE
                else -> binding.progressBar.visibility = View.GONE
            }
        })

        viewModel.editEnvAction.observe(viewLifecycleOwner, Observer {
            if (it.status == Status.SUCCESS) {
                var tokens = it.data!!
                val name = tokens[2]
                tokens = tokens.subList(6, tokens.size - 1)
                showEditDialog(name, tokens.size, tokens)
            }
        })
    }

    private fun populateServices() {
        binding.pickService.adapter = spinnerAdapter
        binding.pickService.setSelection(1)
        binding.pickService.onItemSelectedListener = this
        binding.servicesCards.adapter = serviceCardAdapter
        binding.servicesCards.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                Log.d("SELECTED", "onPageSelected: ")
                scrolled = true
                val pos = position + countHeadersBefore(position + 1)
                binding.pickService.setSelection(pos)
                scrolled = false
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun handleMore(output:String){
//        if (editEnv) {
//            var tokens = output.split(" ")
//            val name = tokens[2]
//            tokens = tokens.subList(6, tokens.size - 1)
//            showEditDialog(name, tokens.size, tokens)
//        }
//        } else {
//            if (output.contains("service autorun set")) {
//                Toast.makeText(context, "Switched autorun", Toast.LENGTH_SHORT).show()
//            }
//        }
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

    private fun openURL(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://$url"))
        Log.d("OPENING: ", "http://$url||")
        val title = "Select a browser"
        val chooser = Intent.createChooser(intent, title)
        if (intent.resolveActivity(requireContext().packageManager) != null) startActivity(chooser)
    }
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
        if (binding.pickService.selectedItem != null && viewModel.selectedService.value != binding.pickService.selectedItem as ServiceInfo) {
            viewModel.selectedService.value = binding.pickService.selectedItem as ServiceInfo
        }

    }

//    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
//    override fun onPageSelected(position: Int) {
//        Log.d("SELECTED", "onPageSelected: ")
//        scrolled = true
//        val pos = position + countHeadersBefore(position + 1)
//        binding.pickService.setSelection(pos)
//        scrolled = false
//    }
//
//    override fun onPageScrollStateChanged(state: Int) {}
    private fun countHeadersBefore(position: Int): Int {
        var count = 0
        for (i in 0..position) {
            if (viewModel.formattedServices[i].isHeader) count++
        }
        return count
    }

//    private fun setOnClick(v: View, command: String, alertDialog: AlertDialog) {
//        v.setOnClickListener {
//            viewModel.sendMessage(command)
//            alertDialog.dismiss()
//            binding.progressBar.visibility = View.VISIBLE
//        }
//    }

//    private fun onLink(selected: ServiceInfo?) {
//        //reqUrls();
//        val chooseBind = DialogChooseUrlBinding.inflate(layoutInflater)
//        val alertDialog = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle)).setView(chooseBind.root).setTitle("Select URL type").create()
//        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
//        setOnClick(chooseBind.localButton, getString(R.string.TREEHOUSES_SERVICES_URL_LOCAL, selected!!.name), alertDialog)
//        setOnClick(chooseBind.torButton, getString(R.string.TREEHOUSES_SERVICES_URL_TOR, selected.name), alertDialog)
//        alertDialog.show()
//    }

    private fun setScreenState(state: Boolean) {
        binding.servicesCards.setPagingEnabled(state)
        binding.pickService.isEnabled = state
        if (state) binding.progressBar.visibility = View.GONE
        else binding.progressBar.visibility = View.VISIBLE
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
                    viewModel.sendMessage(command)
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
    }

    override fun onClickLink(s: ServiceInfo?) {
        val chooseBind = DialogChooseUrlBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle)).setView(chooseBind.root).setTitle("Select URL type").create()
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        chooseBind.localButton.setOnClickListener { viewModel.getLocalLink(s!!); alertDialog.dismiss() }
        chooseBind.torButton.setOnClickListener { viewModel.getTorLink(s!!); alertDialog.dismiss() }
        alertDialog.show()
    }

    override fun onClickEditEnvVar(s: ServiceInfo?) {
        if (s == null) return
        viewModel.editEnvVariableRequest(s)
    }

    override fun onClickAutorun(s: ServiceInfo?, newAutoRun: Boolean) {
        if (s == null) return
        viewModel.switchAutoRun(s, newAutoRun)
        Toast.makeText(context, "Switching autorun status to $newAutoRun", Toast.LENGTH_SHORT).show()
    }

}