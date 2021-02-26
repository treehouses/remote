package io.treehouses.remote.fragments

import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseTunnelSSHFragment
import io.treehouses.remote.utils.DialogUtils

open class TunnelSSHFunctions: BaseTunnelSSHFragment() {

    protected fun addPortListListener() {
        portList!!.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            if (portsName!!.size > 1 && position == portsName!!.size - 1) {
                DialogUtils.createAlertDialog(context, "Delete All Hosts and Ports?") { writeMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_REMOVE_ALL)) }
            }
        }
    }

    protected fun switchButton(isChecked: Boolean) {
        bind!!.switchNotification.isEnabled = false
        if (isChecked) writeMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE_ON))
        else writeMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE_OFF))
    }

    protected fun addingHostButton() {
        if (inputPortNumber.text.toString().isNotEmpty() && inputUserName.text.toString().isNotEmpty() && inputDomainIP.text.toString().isNotEmpty()) {
            val m1 = inputPortNumber.text.toString()
            val m2 = inputUserName.text.toString() + "@" + inputDomainIP.text.toString()
            writeMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_ADD_HOST, m1, m2))
            addHostButton!!.text = "Adding......"
            addHostButton!!.isEnabled = false
            dialogHosts.dismiss()
        }
    }

    protected fun addingPortButton() {
        if (inputExternal.text!!.isNotEmpty() && inputInternal.text!!.isNotEmpty()) {
            val s1 = inputInternal.text.toString()
            val s2 = inputExternal.text.toString()
            val parts = dropdown?.selectedItem.toString().split(":")[0]
            writeMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_ADD_PORT_ACTUAL, s2, s1, parts))
            addPortButton!!.text = "Adding......"
            addPortButton!!.isEnabled = false
            dialog.dismiss()
        }
    }


}