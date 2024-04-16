package io.treehouses.remote.ui.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.adapter.ServiceCardAdapter
import io.treehouses.remote.adapter.ServicesListAdapter
import io.treehouses.remote.databinding.ActivityServicesDetailsBinding
import io.treehouses.remote.databinding.EnvVarBinding
import io.treehouses.remote.databinding.EnvVarItemBinding
import io.treehouses.remote.pojo.ServiceInfo
import io.treehouses.remote.pojo.enum.Resource
import io.treehouses.remote.pojo.enum.Status
import io.treehouses.remote.utils.DialogUtils
import io.treehouses.remote.utils.countHeadersBefore
import io.treehouses.remote.utils.indexOfService
import io.treehouses.remote.utils.logE

class ServicesDetailsFragment : BaseServicesDetailsFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivityServicesDetailsBinding.inflate(inflater, container, false)

        viewModel.servicesData.observe(viewLifecycleOwner, Observer {
            if (it.status == Status.SUCCESS) {
                spinnerAdapter = ServicesListAdapter(requireContext(), viewModel.formattedServices, ContextCompat.getColor(requireContext(), R.color.md_grey_600))
                serviceCardAdapter = ServiceCardAdapter(childFragmentManager, viewModel.formattedServices)
                initialize()
                goToSelected()
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Tutorials.servicesDetailsTutorials(binding, requireActivity())

        viewModel.selectedService.observe(viewLifecycleOwner, Observer { goToSelected() })

        observeServiceAction()

        viewModel.error.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.error.value = ""
            }
            setScreenState(true)
        })
        observeAutoRun()
        observeMoreActions()
    }

    /**
     * Observe Start, Stop, Install, Uninstall actions
     */
    private fun observeServiceAction() {
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
                else -> logE("UNKNOWNRECEIVED in ServiceAction: ${it.status}")
            }
            setScreenState(true)
        })
    }

    /**
     * When the autorun has changed values
     */
    private fun observeAutoRun() {
        viewModel.autoRunAction.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> {
                    setScreenState(false)
                    return@Observer
                }
                Status.SUCCESS -> {
                    Toast.makeText(context, "Switched autorun to ${it.data}", Toast.LENGTH_SHORT).show()
                    viewModel.autoRunAction.value = Resource.nothing()
                }
                else -> logE("UNKNOWN RECEIVED in AutoRun boolean")
            }
            setScreenState(true)
        })
    }

    /**
     * Observe Tor/Local links
     * Observe the fetching of environment variables as well
     */
    private fun observeMoreActions() {

        viewModel.getLinkAction.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    openURL(it.data.toString())
                    binding.progressBar.visibility = View.GONE
                    viewModel.getLinkAction.value = Resource.nothing()
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
                viewModel.editEnvAction.value = Resource.nothing()
            }
        })
    }

    /**
     * Initializes the adapter and the on change listener for the spinner, and the ViewPager
     */
    private fun initialize() {
        binding.pickService.adapter = spinnerAdapter
        binding.pickService.setSelection(1)
        binding.pickService.onItemSelectedListener = this
        binding.servicesCards.adapter = serviceCardAdapter
        binding.servicesCards.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                scrolled = true
                val pos = position + countHeadersBefore(position + 1, viewModel.formattedServices)
                binding.pickService.setSelection(pos)
                scrolled = false
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    /**
     * Goes to the selected service
     * Accounts for spinner/Viewpager discrepancies
     */
    private fun goToSelected() {
        if (viewModel.selectedService.value == null) return
        val pos = indexOfService(viewModel.selectedService.value!!.name, viewModel.formattedServices)
        if (binding.pickService.selectedItemPosition != pos) {
            binding.pickService.setSelection(pos)
        }
        val count = countHeadersBefore(pos, viewModel.formattedServices)
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

    /**
     * Sets the state of the screen
     */
    private fun setScreenState(state: Boolean) {
        binding.servicesCards.setPagingEnabled(state)
        binding.pickService.isEnabled = state
        if (state) binding.progressBar.visibility = View.GONE
        else binding.progressBar.visibility = View.VISIBLE
    }

    /**
     * Show the edit Environment Variable dialog
     * @param name : String = Name of Service
     * @param vars : List<String> = The variables that can be configured
     */
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
        createEditDialog(dialogBinding.root, name, size)
    }

    /**
     * Creates the Edit Env Config dialog
     * @param view = View of the AlertDialog
     * @param name = Name of service
     * @param size = # of Variables
     */
    private fun createEditDialog(view: View, name: String, size: Int) {
        val builder = DialogUtils.createAlertDialog(context, "Edit variables", view, R.drawable.dialog_icon)
        return DialogUtils.createAdvancedDialog(builder, Pair("Edit", "Cancel"),
        {
            var command = "treehouses services $name config edit send"

            for (i in 0 until size) command += " \"" + view.findViewById<TextInputEditText>(i).text + "\""

            viewModel.sendMessage(command)
            Toast.makeText(context, "Environment variables changed", Toast.LENGTH_LONG).show()
        })
    }

}