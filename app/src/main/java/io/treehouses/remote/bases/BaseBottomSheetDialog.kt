package io.treehouses.remote.bases

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.treehouses.remote.callback.HomeInteractListener

class BaseBottomSheetDialog : BottomSheetDialogFragment() {
    protected var context: Context? = null
    protected var listener: HomeInteractListener? = null
    override fun onAttach(c: Context) {
        super.onAttach(c)
        try {
            listener = c as HomeInteractListener
            context = c
        } catch (e: ClassCastException) {
            throw ClassCastException("$c must implement HomeInteractListener")
        }
    }
}