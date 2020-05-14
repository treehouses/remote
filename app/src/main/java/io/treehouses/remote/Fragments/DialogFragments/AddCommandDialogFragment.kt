package io.treehouses.remote.Fragments.DialogFragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import io.treehouses.remote.R
import io.treehouses.remote.pojo.CommandListItem
import io.treehouses.remote.utils.SaveUtils

class AddCommandDialogFragment : DialogFragment() {
    private var commandTitle: EditText? = null
    private var commandValue: EditText? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity!!.layoutInflater
        val mView = inflater.inflate(R.layout.dialog_add_command, null)
        initLayoutView(mView)
        val mDialog = getAlertDialog(mView)
        mDialog.setTitle(R.string.add_command_title)
        return mDialog
    }

    private fun getAlertDialog(mView: View): AlertDialog {
        return AlertDialog.Builder(activity)
                .setView(mView)
                .setTitle(R.string.change_password)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Add Command"
                ) { dialog, which ->
                    if (commandTitle!!.text.toString().length > 0 && commandValue!!.text.toString().length > 0) {
                        SaveUtils.addToCommandsList(context,
                                CommandListItem(commandTitle!!.text.toString(), commandValue!!.text.toString()))
                        done()
                        dismiss()
                    } else {
                        Toast.makeText(context, "Please Enter Text", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton(R.string.cancel) { dialog, which -> dismiss() }.create()
    }

    private fun done() {
        val intent = Intent()
        intent.putExtra("done", true)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
    }

    //initialize views
    private fun initLayoutView(mView: View) {
        commandTitle = mView.findViewById(R.id.commandName)
        commandValue = mView.findViewById(R.id.commandValue)
    }

    companion object {
        private const val TAG = "AddCommandDialogFragment"
        fun newInstance(): AddCommandDialogFragment {
            return AddCommandDialogFragment()
        }
    }
}