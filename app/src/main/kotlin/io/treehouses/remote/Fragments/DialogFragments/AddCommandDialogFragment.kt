package io.treehouses.remote.Fragments.DialogFragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import io.treehouses.remote.R
import io.treehouses.remote.databinding.DialogAddCommandBinding
import io.treehouses.remote.pojo.CommandListItem
import io.treehouses.remote.utils.SaveUtils

class AddCommandDialogFragment : DialogFragment() {

    var bind: DialogAddCommandBinding? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bind = DialogAddCommandBinding.inflate(requireActivity().layoutInflater)
        val mDialog = getAlertDialog(bind!!.root)
        mDialog.setTitle(R.string.add_command_title)
        mDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        return mDialog
    }

    private fun getAlertDialog(mView: View): AlertDialog {
        return AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle))
                .setView(mView)
                .setTitle(R.string.change_password)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Add Command"
                ) { _: DialogInterface?, _: Int ->
                    if (bind!!.commandName.text.toString().isNotEmpty() && bind!!.commandValue.text.toString().isNotEmpty()) {
                        SaveUtils.addToCommandsList(requireContext(),
                                CommandListItem(bind!!.commandName.text.toString(), bind!!.commandValue.text.toString()))
                        done()
                        dismiss()
                    } else {
                        Toast.makeText(context, "Please Enter Text", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int -> dismiss() }.create()
    }

    private fun done() {
        val intent = Intent()
        intent.putExtra("done", true)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
    }

    companion object {
        private const val TAG = "AddCommandDialogFragment"
        fun newInstance(): AddCommandDialogFragment {
            return AddCommandDialogFragment()
        }
    }
}