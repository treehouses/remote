package io.treehouses.remote.ui.network.bottomsheetdialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputLayout
import io.treehouses.remote.Constants
import io.treehouses.remote.fragments.dialogfragments.WifiDialogFragment
import io.treehouses.remote.ui.network.NetworkFragment
import io.treehouses.remote.ui.network.NetworkFragment.Companion.openWifiDialog
import io.treehouses.remote.fragments.TextBoxValidation
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.databinding.DialogWifiBinding
import io.treehouses.remote.ui.network.NetworkViewModel

class WifiBottomSheet : BaseBottomSheetDialog() {
    protected val viewModel: NetworkViewModel by viewModels(ownerProducer = { this })
    private lateinit var bind: DialogWifiBinding
    private lateinit var array: MutableList<Any>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogWifiBinding.inflate(inflater, container, false)
        bind.btnStartConfig.setOnClickListener {
            viewModel.wifiStartConfigListener(bind.checkBoxHiddenWifi.isChecked, bind.checkBoxEnterprise.isChecked,
                    bind.editTextSSID, bind.wifipassword, bind.wifiUsername)
            val intent = Intent()
            intent.putExtra(NetworkFragment.CLICKED_START_CONFIG, true)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        }
        bind.setWifiProfile.setOnClickListener {
            viewModel.wifiSetAddProfileListener(bind.editTextSSID.text.toString(), bind.wifipassword.text.toString(),
                    bind.checkBoxHiddenWifi.isChecked)
        }
        hiddenOrEnterprise()
        bind.btnWifiSearch.setOnClickListener { openWifiDialog(this@WifiBottomSheet, context) }
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


    fun hiddenOrEnterprise() {
        bind.checkBoxEnterprise.setOnCheckedChangeListener {_, isChecked ->
            bind.enterpriseLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }
}