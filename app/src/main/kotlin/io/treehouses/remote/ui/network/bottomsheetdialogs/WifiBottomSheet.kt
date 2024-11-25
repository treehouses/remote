package io.treehouses.remote.ui.network.bottomsheetdialogs

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import io.treehouses.remote.R
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import io.treehouses.remote.Constants
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.databinding.DialogWifiBinding
import io.treehouses.remote.fragments.TextBoxValidation
import io.treehouses.remote.fragments.dialogfragments.WifiDialogFragment
import io.treehouses.remote.ui.network.NetworkFragment.Companion.openWifiDialog
import io.treehouses.remote.ui.network.NetworkViewModel

open class WifiBottomSheet : BaseBottomSheetDialog() {
    protected val viewModel: NetworkViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private lateinit var bind: DialogWifiBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        viewModel.wifiUserError.observe(viewLifecycleOwner) {
            bind.wifiUsername.error = if (it) "Please enter a username" else null
        }
        viewModel.checkBoxChecked.observe(viewLifecycleOwner) { visible ->
            bind.enterpriseLayout.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    private fun setClickListeners() {
        bind.btnStartConfig.setOnClickListener {
            val booleanMap = mapOf("checkBoxHiddenWifi" to bind.checkBoxHiddenWifi.isChecked, "checkBoxEnterprise" to bind.checkBoxEnterprise.isChecked)
            viewModel.sendWifiMessage(booleanMap, "${bind.editTextSSID.text}", "${bind.wifipassword.text}", "${bind.wifiUsername.text}")
            dismiss()
        }
        bind.setWifiProfile.setOnClickListener {
            viewModel.wifiSetAddProfileListener("${bind.editTextSSID.text}", "${bind.wifipassword.text}", bind.checkBoxHiddenWifi.isChecked)
        }
        bind.checkBoxEnterprise.setOnCheckedChangeListener { _, isChecked ->
            viewModel.hiddenOrEnterprise(isChecked)
        }
        bind.btnWifiSearch.setOnClickListener {
            val locationManager = context?.getSystemService(LOCATION_SERVICE) as LocationManager

            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showLocationPermissionRationale()
                } else {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                }
            } else {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context?.startActivity(intent)
                } else {
                    openWifiDialog(this@WifiBottomSheet, context)
                }
            }
        }
    }

    private fun showLocationPermissionRationale() {
        AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
            .setTitle("Location Permission Needed")
            .setMessage("This app requires location access to enable WiFi search functionality. Please grant location permission to continue.")
            .setPositiveButton("OK") { _, _ ->
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                bind.btnWifiSearch.performClick()
            } else {
                Toast.makeText(context, "Location permission is required to enable WiFi search", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
