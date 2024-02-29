package io.treehouses.remote.fragments.dialogfragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import io.treehouses.remote.R
import io.treehouses.remote.adapter.BluetoothTroubleshootAdapter
import io.treehouses.remote.bases.FullScreenDialogFragment
import io.treehouses.remote.databinding.DialogBluetoothTroubleshootingBinding

class BluetoothFailedDialogFragment : FullScreenDialogFragment() {
    lateinit var bind : DialogBluetoothTroubleshootingBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogBluetoothTroubleshootingBinding.inflate(inflater, container, false)
        dialog!!.window!!.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 30))
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind.closeButton.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putBoolean(DONT_SHOW_DIALOG, bind.showAgain.isChecked).apply()
            dismiss()
        }

        bind.showAgain.isChecked = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(DONT_SHOW_DIALOG, false)
        val questions = resources.getStringArray(R.array.faq_issues).toList()
        val answers = resources.getStringArray(R.array.faq_answers).toList()
        bind.faqList.setAdapter(BluetoothTroubleshootAdapter(requireContext(),questions, answers))
        bind.errorDisplay.text = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("connectError", "")
    }

    companion object {
        const val DONT_SHOW_DIALOG = "show_dialog_preference"
    }
}