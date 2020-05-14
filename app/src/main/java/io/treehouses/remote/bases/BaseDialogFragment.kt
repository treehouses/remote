package io.treehouses.remote.bases

import android.content.Context
import androidx.fragment.app.DialogFragment
import io.treehouses.remote.callback.HomeInteractListener

class BaseDialogFragment : DialogFragment() {
    var listener: HomeInteractListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HomeInteractListener) listener = context as HomeInteractListener
    }
}