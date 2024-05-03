package io.treehouses.remote.bases

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup

open class FullScreenDialogFragment : BaseDialogFragment() {

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}