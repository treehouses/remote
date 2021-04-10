package io.treehouses.remote.ui.network.bottomsheetdialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.treehouses.remote.Constants
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.databinding.DialogWifiBinding
import io.treehouses.remote.fragments.TextBoxValidation
import io.treehouses.remote.fragments.dialogfragments.WifiDialogFragment
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.ui.network.NetworkFragment.Companion.openWifiDialog
import io.treehouses.remote.ui.network.NetworkViewModel

class WifiBottomSheet : BaseBottomSheetDialog() {
    protected val viewModel: NetworkViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private lateinit var bind: DialogWifiBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogWifiBinding.inflate(inflater, container, false)
        setObservers()
        setClickListeners()
        val validation = TextBoxValidation(requireContext(), bind.editTextSSID, bind.wifipassword, "wifi")
        validation.setStart(bind.btnStartConfig)
        validation.setAddprofile(bind.setWifiProfile)
        validation.setTextInputLayout(bind.textInputLayout)
        return bind.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_DIALOG_WIFI) {
            bind.editTextSSID.setText(data?.getStringExtra(WifiDialogFragment.WIFI_SSID_KEY))
        }
    }

    private fun setObservers() {
        viewModel.wifiUserError.observe(viewLifecycleOwner, Observer {
            bind.wifiUsername.error = if (it) "Please enter a username" else null
        })
        viewModel.checkBoxChecked.observe(viewLifecycleOwner, Observer { visible ->
            bind.enterpriseLayout.visibility = if (visible) View.VISIBLE else View.GONE
        })
    }

    private fun setClickListeners() {
        bind.btnStartConfig.setOnClickListener {
            val booleanMap = mapOf("checkBoxHiddenWifi" to bind.checkBoxHiddenWifi.isChecked,
                    "checkBoxEnterprise" to bind.checkBoxEnterprise.isChecked)
            viewModel.sendWifiMessage(booleanMap, bind.editTextSSID.text.toString(),
                    bind.wifipassword.text.toString(), bind.wifiUsername.text.toString())
            dismiss()
        }
        bind.setWifiProfile.setOnClickListener {
            val networkProfile = NetworkProfile(bind.editTextSSID.text.toString(), bind.wifipassword.text.toString(),
                    bind.checkBoxHiddenWifi.isChecked)
            viewModel.bridgeHotspotWifiSetAddProfileListener("Wifi Profile Added", networkProfile)
//            viewModel.wifiSetAddProfileListener(bind.editTextSSID.text.toString(), bind.wifipassword.text.toString(),
//                    bind.checkBoxHiddenWifi.isChecked)
        }
        bind.checkBoxEnterprise.setOnCheckedChangeListener { _, isChecked ->
            viewModel.hiddenOrEnterprise(isChecked)
        }
        bind.btnWifiSearch.setOnClickListener { openWifiDialog(this@WifiBottomSheet, context) }
    }
}
