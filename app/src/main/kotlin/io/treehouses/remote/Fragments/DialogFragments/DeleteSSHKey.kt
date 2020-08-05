package io.treehouses.remote.Fragments.DialogFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import io.treehouses.remote.bases.FullScreenDialogFragment
import io.treehouses.remote.databinding.DialogDeleteSshKeyBinding
import io.treehouses.remote.utils.KeyUtils

class DeleteSSHKey : FullScreenDialogFragment() {
    companion object {
        const val KEY_TO_DELETE = "KEY_TO_DELETE"
    }
    private lateinit var bind : DialogDeleteSshKeyBinding
    private lateinit var keyToDelete : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogDeleteSshKeyBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        keyToDelete = arguments?.getString(KEY_TO_DELETE)!!
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val titleText = "Delete $keyToDelete?"
        bind.title.text = titleText

        bind.deleteKeyConfirmation.hint = keyToDelete
        bind.deleteKeyConfirmation.addTextChangedListener {
            setEnabled(it?.toString() == keyToDelete)
        }

        setEnabled(false)
        bind.deleteButton.setOnClickListener {
            if (bind.deleteKeyConfirmation.text.toString() == keyToDelete) {
                KeyUtils.deleteKey(requireContext(), keyToDelete)
                Toast.makeText(requireContext(), "Deleted $keyToDelete. Please Refresh Screen to see changes.", Toast.LENGTH_LONG).show()
                dismiss()
            }
        }

        bind.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun setEnabled(bool: Boolean) {
        bind.deleteButton.isEnabled = bool
        bind.deleteButton.isClickable = bool
    }

}