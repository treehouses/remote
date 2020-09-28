package io.treehouses.remote.bases

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ListView
import android.widget.Switch
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.adapter.TunnelPortAdapter
import io.treehouses.remote.databinding.ActivityTorFragmentBinding
import io.treehouses.remote.utils.DialogUtils
import io.treehouses.remote.utils.TunnelUtils
import java.util.ArrayList

open class BaseTorTabFragment: BaseFragment() {
    override lateinit var mChatService: BluetoothChatService
    protected var nowButton: Button? = null
    protected var startButton: Button? = null
    protected var addPortButton: Button? = null
    protected var portsName: ArrayList<String>? = null
    protected var adapter: TunnelPortAdapter? = null
    protected var hostName: String = ""
    protected var myClipboard: ClipboardManager? = null
    protected var myClip: ClipData? = null
    protected var portList: ListView? = null
    protected var notification: Switch? = null
    var bind: ActivityTorFragmentBinding? = null

    override fun setUserVisibleHint(visible: Boolean) {
        if (visible) {
            if (isListenerInitialized()) {
                mChatService = listener.getChatService()
                mChatService?.updateHandler(mHandler)
                listener.sendMessage(getString(R.string.TREEHOUSES_TOR_PORTS))
                portsName = ArrayList()
            }

        }
    }

    protected fun isAttachedToActivity(): Boolean {
        return isVisible && activity != null
    }

    protected fun setWindowProperties(dialog: Dialog) {
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val window = dialog.window
        window!!.setGravity(Gravity.CENTER)
        window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    protected fun initializeProperties() {
        bind!!.btnAddPort
        startButton = bind!!.btnTorStart
        addPortButton = bind!!.btnAddPort
        startButton!!.isEnabled = false
        startButton!!.text = "Getting Tor Status from raspberry pi"
    }

    protected fun promptDeleteAllPorts() {
        DialogUtils.createAlertDialog(context, "Delete All Ports?") { listener.sendMessage(getString(R.string.TREEHOUSES_TOR_DELETE_ALL)) }
    }

    protected fun promptDeletePort(position: Int) {
        DialogUtils.createAlertDialog(context, "Delete Port " + portsName!![position] + " ?")
        {
            val msg = getString(R.string.TREEHOUSES_TOR_DELETE, TunnelUtils.getPortName(portsName, position))
            listener.sendMessage(msg)
            addPortButton!!.text = "Deleting port. Please wait..."
            portList!!.isEnabled = false
            addPortButton!!.isEnabled = false
        }
    }

}