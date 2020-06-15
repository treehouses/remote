package io.treehouses.remote.Fragments.DialogFragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.ParseDbService
import io.treehouses.remote.R
import io.treehouses.remote.Views.RecyclerViewClickListener
import io.treehouses.remote.adapter.HelpAdapter
import io.treehouses.remote.databinding.DialogFeedbackBinding
import java.util.HashMap


class FeedbackDialog : DialogFragment() {
    private lateinit var bind: DialogFeedbackBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogFeedbackBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind.btnSendFeedback.setOnClickListener {
            if (notBlank()) {
                val map = HashMap<String, String?>()
                map["name"] = bind.editName.text.toString()
                map["email"] = bind.editEmail.text.toString()
                map["phoneNumber"] = bind.editPhoneNumber.text.toString()
                map["feedbackType"] = if (bind.radioButtonBug.isChecked) "bug" else "suggestion"
                map["message"] = bind.editMessage.text.toString()
                ParseDbService.sendFeedback(activity, map)
                dismiss()
            } else {
                Toast.makeText(context, "Name, email, type, and message are required fields", Toast.LENGTH_LONG).show()
            }
        }
        bind.btnCancel.setOnClickListener {
            dismiss()
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun notBlank(): Boolean{
        return bind.editName.text.toString().isNotBlank() &&
                bind.editEmail.text.toString().isNotBlank() &&
                bind.editMessage.text.toString().isNotBlank() &&
                (bind.radioButtonBug.isChecked || bind.radioButtonSuggestion.isChecked)
    }
}
