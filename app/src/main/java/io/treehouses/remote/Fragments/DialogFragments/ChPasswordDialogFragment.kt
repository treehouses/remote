package io.treehouses.remote.Fragments.DialogFragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import io.treehouses.remote.Fragments.TextBoxValidation
import io.treehouses.remote.R

/**
 * Created by going-gone on 4/19/2018.
 */
class ChPasswordDialogFragment : DialogFragment() {
    private var passwordEditText: EditText? = null
    private var confirmPassEditText: EditText? = null
    private val textBoxValidation: TextBoxValidation = TextBoxValidation()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "In onCreateDialog()")

        // Build the dialog and set up the button click handlers
        val inflater = activity!!.layoutInflater
        val mView = inflater.inflate(R.layout.chpass_dialog, null)
        initLayoutView(mView)
        val mDialog = getAlertDialog(mView)
        mDialog.setTitle(R.string.change_password)

        //initially disable button click
        textBoxValidation.getListener(mDialog)
        setTextChangeListener(mDialog)
        return mDialog
    }

    //creates the dialog for the change password dialog
    protected fun getAlertDialog(mView: View?): AlertDialog {
        return AlertDialog.Builder(activity)
                .setView(mView)
                .setTitle(R.string.change_password)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.change_password
                ) { dialog: DialogInterface, whichButton: Int ->
                    dialog.dismiss()
                    val chPass = passwordEditText!!.text.toString()
                    val i = Intent()
                    i.putExtra("type", "chPass")
                    i.putExtra("password", chPass)
                    targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, i)
                }
                .setNegativeButton(R.string.cancel) { dialog: DialogInterface?, whichButton: Int -> targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, activity!!.intent) }
                .create()
    }

    //initialize the view
    private fun initLayoutView(mView: View) {
        passwordEditText = mView.findViewById(R.id.changePassword)
        confirmPassEditText = mView.findViewById(R.id.confirmPassword)
    }

    //listener for text change within this dialog
    private fun setTextChangeListener(mDialog: AlertDialog) {
        textBoxValidation.setmDialog(mDialog)
        textBoxValidation.setTextWatcher(passwordEditText)
        textBoxValidation.PWD = passwordEditText
        textBoxValidation.changePWValidation(confirmPassEditText, activity)
    }

    companion object {
        private const val TAG = "ChPasswordDialogFragment"
        fun newInstance(): ChPasswordDialogFragment {
            return ChPasswordDialogFragment()
        }
    }
}