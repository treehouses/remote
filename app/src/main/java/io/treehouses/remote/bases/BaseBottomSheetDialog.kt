package io.treehouses.remote.bases

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener

open class BaseBottomSheetDialog : BottomSheetDialogFragment() {
    protected lateinit var listener: HomeInteractListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onAttach(c: Context) {
        super.onAttach(c)
        try {
            listener = c as HomeInteractListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$c must implement HomeInteractListener")
        }
    }
}