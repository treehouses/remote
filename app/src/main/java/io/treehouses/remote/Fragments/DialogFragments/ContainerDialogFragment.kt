package io.treehouses.remote.Fragments.DialogFragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import io.treehouses.remote.R
import java.util.*

class ContainerDialogFragment : DialogFragment() {
    // Layout Views
    private var mSpinner: Spinner? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "In onCreateDialog()")

        // Build the dialog and set up the button click handlers
        val inflater = activity!!.layoutInflater
        val mView = inflater.inflate(R.layout.dialog_container, null)
        initLayoutView(mView)
        val list = ArrayList<String>()
        list.add("None")
        list.add("Docker")
        list.add("Balena")
        val adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinner!!.adapter = adapter
        return getAlertDialog(mView)
    }

    private fun getAlertDialog(mView: View): AlertDialog {
        return AlertDialog.Builder(activity)
                .setView(mView)
                .setTitle(R.string.dialog_message)
                .setIcon(R.drawable.dialog_icon)
                .setPositiveButton(R.string.start_configuration
                ) { dialog: DialogInterface?, whichButton: Int ->
                    val container = mSpinner!!.selectedItem.toString()
                    val intent = Intent()
                    intent.putExtra("container", container)
                    intent.putExtra("type", "container")
                    targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
                }
                .setNegativeButton(R.string.cancel) { dialog: DialogInterface?, whichButton: Int -> targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, activity!!.intent) }
                .create()
    }

    private fun initLayoutView(mView: View) {
        mSpinner = mView.findViewById(R.id.spinner)
    }

    companion object {
        private const val TAG = "ContainerDialogFragment"
        fun newInstance(num: Int): ContainerDialogFragment {

            //        Bundle bundle = new Bundle();
//        bundle.putInt("num", num);
//        dialogFragment.setArguments(bundle);
            return ContainerDialogFragment()
        }
    }
}