package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import io.treehouses.remote.Fragments.NewNetworkFragment.CLICKED_START_CONFIG
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseBottomSheetDialog

class EthernetBottomSheet : BaseBottomSheetDialog() {
    private var etIP: EditText? = null
    private var etMask: EditText? = null
    private var etGateway: EditText? = null
    private var DNSText: EditText? = null
    private var startConfig: Button? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_ethernet, container, false)
        etIP = v.findViewById(R.id.ip)
        etMask = v.findViewById(R.id.mask)
        etGateway = v.findViewById(R.id.gateway)
        DNSText = v.findViewById(R.id.dns)
        startConfig = v.findViewById(R.id.btn_start_config)
        startConfig.setOnClickListener(View.OnClickListener { v1: View? ->
            val ip = etIP.getText().toString()
            val dns = DNSText.getText().toString()
            val gateway = etGateway.getText().toString()
            val mask = etMask.getText().toString()
            listener.sendMessage(String.format("treehouses ethernet %s %s %s %s", ip, mask, gateway, dns))
            val intent = Intent()
            intent.putExtra(CLICKED_START_CONFIG, true)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        })
        return v
    }
}