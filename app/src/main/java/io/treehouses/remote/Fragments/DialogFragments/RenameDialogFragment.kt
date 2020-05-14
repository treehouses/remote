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

class RenameDialogFragment : DialogFragment() {
    // Layout Views
    private var mHostNameEditText: EditText? = null
    var textboxValidation: TextBoxValidation = TextBoxValidation()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "In onCreateDialog()")

        // Build the dialog and set up the button click handlers
        val inflater = activity!!.layoutInflater
        val mView = inflater.inflate(R.layout.dialog_rename, null)
        initLayoutView(mView)
        val mDialog = getAlertDialog(mView)

        //initially disable button click
        textboxValidation.getListener(mDialog)
        setTextChangeListener(mDialog)
        return mDialog
    }

    private fun getAlertDialog(mView: View): AlertDialog {
        return AlertDialog.Builder(activity)
                .setView(mView)
                .setTitle(R.string.dialog_message)
                .setMessage(R.string.rename_message)
                .setIcon(R.drawable.dialog_icon)
                .setPositiveButton(R.string.start_configuration
                ) { dialog: DialogInterface?, whichButton: Int ->
                    val intent = Intent()
                    val hostname = mHostNameEditText!!.text.toString()
                    intent.putExtra("hostname", hostname)
                    intent.putExtra("type", "rename")
                    targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
                }
                .setNegativeButton(R.string.cancel) { dialog: DialogInterface?, whichButton: Int -> targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, activity!!.intent) }
                .create()
    }

    private fun setTextChangeListener(mDialog: AlertDialog) {
        textboxValidation.setmDialog(mDialog)
        textboxValidation.setTextWatcher(mHostNameEditText)
        textboxValidation.SSID = mHostNameEditText
    }

    private fun initLayoutView(mView: View) {
        mHostNameEditText = mView.findViewById(R.id.hostname)
    }

    companion object {
        private const val TAG = "RenameDialogFragment"
        fun newInstance(num: Int): RenameDialogFragment {
            //        Bundle bundle = new Bundle();
//        bundle.putInt("num", num);
//        dialogFragment.setArguments(bundle);
            return RenameDialogFragment()
        }
    }
}