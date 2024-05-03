package io.treehouses.remote.fragments.dialogfragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import io.treehouses.remote.network.ParseDbService
import io.treehouses.remote.bases.FullScreenDialogFragment
import io.treehouses.remote.databinding.DialogFeedbackBinding
import java.util.*


class FeedbackDialogFragment : FullScreenDialogFragment() {
    private lateinit var bind: DialogFeedbackBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = DialogFeedbackBinding.inflate(inflater, container, false)

        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 20)
        dialog!!.window!!.setBackgroundDrawable(inset)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        bind.editEmail.addTextChangedListener {
//            if(!Patterns.EMAIL_ADDRESS.matcher(it.toString()).matches())
//                bind.editEmail.error = "Enter a valid email"
//        }

        feedbackFormButtonListeners()
    }

    private fun feedbackFormButtonListeners(){
        bind.btnSendFeedback.setOnClickListener {
            if (notBlank()) {
//                && bind.editEmail.error == null && bind.editPhoneNumber.error == null
                val map = HashMap<String, String>()
                map["name"] = bind.editName.text.toString()
//                map["email"] = bind.editEmail.text.toString()
//                map["phoneNumber"] = bind.editPhoneNumber.text.toString()
                map["feedbackType"] = if (bind.radioButtonBug.isChecked) "bug" else "suggestion"
                map["message"] = bind.editMessage.text.toString()
                ParseDbService.sendFeedback(map)
                Toast.makeText(context, "Feedback sent successfully", Toast.LENGTH_LONG).show()
                dismiss()
            } else {
                Toast.makeText(context, "Name, message", Toast.LENGTH_LONG).show()
//                , message type, and one contact source are required.
            }
        }

        bind.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun notBlank(): Boolean {
        return bind.editName.text.toString().isNotBlank() &&
//                (bind.editEmail.text.toString().isNotBlank() || bind.editPhoneNumber.text.toString().isNotBlank()) &&
                bind.editMessage.text.toString().isNotBlank() &&
                (bind.radioButtonBug.isChecked || bind.radioButtonSuggestion.isChecked)
    }
}
