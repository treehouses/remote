package io.treehouses.remote.bases

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.treehouses.remote.callback.HomeInteractListener

open class BaseBottomSheetDialog : BottomSheetDialogFragment() {
    protected lateinit var listener: HomeInteractListener

    override fun onAttach(c: Context) {
        super.onAttach(c)
        try {
            listener = c as HomeInteractListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$c must implement HomeInteractListener")
        }
    }
}