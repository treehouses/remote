package io.treehouses.remote.ui.services

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.fragment.app.viewModels
import io.treehouses.remote.R
import io.treehouses.remote.adapter.ServiceCardAdapter
import io.treehouses.remote.adapter.ServicesListAdapter
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.ServiceActionListener
import io.treehouses.remote.databinding.ActivityServicesDetailsBinding
import io.treehouses.remote.databinding.DialogChooseUrlBinding
import io.treehouses.remote.pojo.ServiceInfo
import io.treehouses.remote.utils.countHeadersBefore
import io.treehouses.remote.utils.logD

open class BaseServicesDetailsFragment: BaseFragment(), OnItemSelectedListener, ServiceActionListener {

    /**
     * Adapter for the spinner to select a service from dropdown
     */
    /**
     * Adapter for the spinner to select a service from dropdown
     */
    protected var spinnerAdapter: ServicesListAdapter? = null

    /**
     * Card adapter for the Service Cards
     */
    /**
     * Card adapter for the Service Cards
     */
    protected var serviceCardAdapter: ServiceCardAdapter? = null

    /**
     * Variable to keep track if the switch of the services was user-initiated, or
     * if it was done programmatically
     * scrolled = true implies programmatically
     */
    /**
     * Variable to keep track if the switch of the services was user-initiated, or
     * if it was done programmatically
     * scrolled = true implies programmatically
     */
    protected var scrolled = false

    protected lateinit var binding: ActivityServicesDetailsBinding
    protected val viewModel by viewModels<ServicesViewModel>(ownerProducer = {requireParentFragment()})

    /**
     * Opens a URL (Tor, or a local one as well)
     */
    /**
     * Opens a URL (Tor, or a local one as well)
     */
    protected fun openURL(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://$url"))
        logD("OPENING: http://$url||")
        val chooser = Intent.createChooser(intent, "Select a browser")
        if (intent.resolveActivity(requireContext().packageManager) != null) startActivity(chooser)
    }

    /**
     * Start/Stop button was clicked in a Service Card
     */
    /**
     * Start/Stop button was clicked in a Service Card
     */
    override fun onClickStart(s: ServiceInfo?) {
        if (s == null) return
        viewModel.onStartClicked(s)
    }

    /**
     * Install/Uninstall was clicked in a Service Card
     */
    /**
     * Install/Uninstall was clicked in a Service Card
     */
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

    /**
     * The open link button was clicked in a Service Card
     */
    /**
     * The open link button was clicked in a Service Card
     */
    override fun onClickLink(s: ServiceInfo?) {
        val chooseBind = DialogChooseUrlBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle)).setView(chooseBind.root).create()
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        chooseBind.closeButton.setOnClickListener { alertDialog.dismiss() }
        chooseBind.localButton.setOnClickListener { viewModel.getLocalLink(s!!); alertDialog.dismiss() }
        chooseBind.torButton.setOnClickListener { viewModel.getTorLink(s!!); alertDialog.dismiss() }
        alertDialog.show()
    }

    /**
     * Edit Environment Variable was clicked in a Service Card
     */
    /**
     * Edit Environment Variable was clicked in a Service Card
     */
    override fun onClickEditEnvVar(s: ServiceInfo?) {
        viewModel.editEnvVariableRequest(s ?: return)
    }

    /**
     * The autorun value was changed in a Service Card
     * @param s : ServiceInfo?
     * @param newAutoRun : Boolean = The new value of the autorun
     */
    /**
     * The autorun value was changed in a Service Card
     * @param s : ServiceInfo?
     * @param newAutoRun : Boolean = The new value of the autorun
     */
    override fun onClickAutorun(s: ServiceInfo?, newAutoRun: Boolean) {
        if (s == null) return
        viewModel.switchAutoRun(s, newAutoRun)
        Toast.makeText(context, "Switching autorun status to $newAutoRun", Toast.LENGTH_SHORT).show()
    }

    /**
     * When an item is selected, make sure it was not scrolled programmatically
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (!scrolled) {
            if (viewModel.formattedServices[position].isHeader) return
            binding.servicesCards.currentItem = position - countHeadersBefore(position, viewModel.formattedServices)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}