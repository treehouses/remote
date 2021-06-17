package io.treehouses.remote.ui.tunnelssh

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.treehouses.remote.adapter.TunnelPortAdapter
import io.treehouses.remote.databinding.ActivityTunnelSshFragmentBinding
import io.treehouses.remote.pojo.TunnelSSHKeyDialogData

open class BaseTunnelSSHFragment : Fragment() {
    protected val viewModel: TunnelSSHViewModel by viewModels(ownerProducer = { this })
    protected lateinit var bind: ActivityTunnelSshFragmentBinding
    var adapter: TunnelPortAdapter? = null
    protected lateinit var adapter2: ArrayAdapter<String>
    var portsName: java.util.ArrayList<String>? = null
    var hostsName: java.util.ArrayList<String>? = null
    lateinit var dialogPort: Dialog
    lateinit var dialogHosts: Dialog
    lateinit var dialogKeys: Dialog

    protected fun handlePiKeySave(profile: String, storedPublicKey: String?, storedPrivateKey: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Save Key To Pi")
        builder.setMessage(
                "Phone Public Key for ${profile}: \n$storedPublicKey\n\n" +
                        "Phone Private Key for ${profile}: \n$storedPrivateKey")
        builder.setPositiveButton("Save to Pi") { _: DialogInterface?, _: Int ->
            viewModel.sendMessage("treehouses remote key receive \"${storedPublicKey}\" \"${storedPrivateKey}\" $profile")
            Toast.makeText(context, "Key saved to Pi successfully", Toast.LENGTH_LONG).show()
        }.setNegativeButton("Cancel") { dialog: DialogInterface?, _: Int -> dialog?.dismiss() }
        builder.show()
    }

    protected fun handlePhoneKeySave(data: TunnelSSHKeyDialogData) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Save Key To Phone")
        builder.setMessage("Pi Public Key for ${data.profile}: \n${data.piPublicKey}\n" +
                "Pi Private Key for ${data.profile}: \n$data.piPrivateKey")
        builder.setPositiveButton("Save to Phone") { _: DialogInterface?, _: Int ->
            viewModel.saveKeyToPhone(data.profile, data.piPublicKey, data.piPrivateKey)
        }
        setNeutralButtonAndShow(builder)
    }


    protected fun handleDifferentKeys(data: TunnelSSHKeyDialogData) {

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Overwrite On Pi or Phone")

        val strPiPublicKey = "Pi Public Key for ${data.profile}: \n${data.piPublicKey}"
        val strPiPrivateKey = "Pi Private Key for ${data.profile}: \n${data.piPrivateKey}"
        val strPhonePublicKey = "Phone Public Key for ${data.profile}: \n${data.storedPublicKey}"
        val strPhonePrivateKey = "Phone Private Key for ${data.profile}: \n${data.storedPrivateKey}"

        val message = ("There are different keys on the Pi and the phone. Would you like to overwrite the Pi's key or the phone's key?\n\n" +
                strPiPublicKey + "\n\n" +
                strPiPrivateKey + "\n\n" +
                strPhonePublicKey + "\n\n" +
                strPhonePrivateKey)

        builder.setMessage(message)
        builder.setPositiveButton("Save to Phone") { _: DialogInterface?, _: Int ->
            viewModel.saveKeyToPhone(data.profile, data.piPublicKey, data.piPrivateKey)
        }
        builder.setNegativeButton("Save to Pi") { _: DialogInterface?, _: Int ->
            viewModel.sendMessage("treehouses remote key receive \"${data.storedPublicKey}\" \"${data.storedPublicKey}\" ${data.profile}")
            Toast.makeText(context, "The Pi's key has been overwritten with the phone's key successfully ", Toast.LENGTH_LONG).show()
        }
        setNeutralButtonAndShow(builder)
    }
    private  fun setNeutralButtonAndShow(builder: AlertDialog.Builder) {
        builder.setNeutralButton("Cancel") { dialog: DialogInterface?, _: Int -> dialog?.dismiss() }
        builder.show()
    }
    protected fun setPortDialog(builder: AlertDialog.Builder, position: Int, title: String) {
        builder.setTitle(title + portsName!![position] + " ?")
        builder.setPositiveButton("Confirm") { dialog, _ ->
            if (title.contains("Host")) viewModel.deleteHost(position)
            else viewModel.deletePort(position)
            dialog.dismiss()
        }
    }

}