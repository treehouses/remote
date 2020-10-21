package io.treehouses.remote.fragments.dialogfragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import androidx.fragment.app.DialogFragment
import io.treehouses.remote.fragments.TextBoxValidation
import io.treehouses.remote.callback.FragmentDialogInterface
import io.treehouses.remote.R
import io.treehouses.remote.databinding.ChpassDialogBinding
import io.treehouses.remote.utils.logD

/**
 * Created by going-gone on 4/19/2018.
 */
class ChPasswordDialogFragment : DialogFragment(), FragmentDialogInterface {
    //    private EditText passwordEditText;
    //    private EditText confirmPassEditText;
    var bind: ChpassDialogBinding? = null
    private val textBoxValidation = TextBoxValidation()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        logD("In onCreateDialog()")
        bind = ChpassDialogBinding.inflate(requireActivity().layoutInflater)
        val mDialog = getAlertDialog(bind!!.root)
        mDialog.setTitle(R.string.change_password)
        mDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        //initially disable button click
        textBoxValidation.getListener(mDialog)
        setTextChangeListener(mDialog)
        return mDialog
    }

    //creates the dialog for the change password dialog
    protected fun getAlertDialog(mView: View?): AlertDialog {
        return createAlertDialog(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle), mView, R.string.change_password, android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.change_password
                ) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    val chPass = bind!!.changePassword.text.toString()
                    val i = Intent()
                    i.putExtra("type", "chPass")
                    i.putExtra("password", chPass)
                    targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, i)
                }
                .setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int -> targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, requireActivity().intent) }
                .create()
    }

    //listener for text change within this dialog
    private fun setTextChangeListener(mDialog: AlertDialog) {
        textBoxValidation.setmDialog(mDialog)
        textBoxValidation.setTextWatcher(bind!!.changePassword)
        textBoxValidation.PWD = bind!!.changePassword
        textBoxValidation.changePWValidation(bind!!.confirmPassword, bind!!.layoutText, requireActivity())
    }

    companion object {
        private const val TAG = "ChPasswordDialogFragment"
        fun newInstance(): ChPasswordDialogFragment {
            return ChPasswordDialogFragment()
        }
    }
}