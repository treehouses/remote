package io.treehouses.remote.Fragments.DialogFragments

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import android.util.Patterns
import androidx.fragment.app.DialogFragment
import io.treehouses.remote.Network.ParseDbService
import io.treehouses.remote.databinding.DialogFeedbackBinding
import io.treehouses.remote.utils.Utils
import java.util.HashMap


class FeedbackDialogFragment : DialogFragment() {
    private lateinit var bind: DialogFeedbackBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogFeedbackBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind.editEmail.addTextChangedListener (object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if(!Patterns.EMAIL_ADDRESS.matcher(bind.editEmail.text.toString()).matches())
                    bind.editEmail.error = "Enter a valid email"
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        feedbackFormButtonListeners()
    }

    private fun feedbackFormButtonListeners(){
        bind.btnSendFeedback.setOnClickListener {
            if (notBlank() && bind.editEmail.error == null && bind.editPhoneNumber.error == null) {
                val map = HashMap<String, String>()
                map["name"] = bind.editName.text.toString()
                map["email"] = bind.editEmail.text.toString()
                map["phoneNumber"] = bind.editPhoneNumber.text.toString()
                map["feedbackType"] = if (bind.radioButtonBug.isChecked) "bug" else "suggestion"
                map["message"] = bind.editMessage.text.toString()
                ParseDbService.sendFeedback(map)
                Toast.makeText(context, "Feedback send successfully", Toast.LENGTH_LONG).show()
                dismiss()
            } else {
                Toast.makeText(context, "Name, email, type, and message are required fields", Toast.LENGTH_LONG).show()
            }
        }

        bind.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun notBlank(): Boolean {
        return bind.editName.text.toString().isNotBlank() &&
                bind.editEmail.text.toString().isNotBlank() &&
                bind.editMessage.text.toString().isNotBlank() &&
                (bind.radioButtonBug.isChecked || bind.radioButtonSuggestion.isChecked)
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
}
