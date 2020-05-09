package io.treehouses.remote.bluetoothv2.base.view

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment(), MVPView {

    private var parentActivity: BaseActivity? = null
    private var progressDialog: ProgressDialog? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity) {
            val activity = context as BaseActivity?
            this.parentActivity = activity
            activity?.onFragmentAttached()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUp()
    }

    override fun hideProgress() {
        if (progressDialog != null && progressDialog?.isShowing!!) {
            progressDialog?.cancel()
        }
    }

    override fun showProgress() {
        hideProgress()
    }

    fun getBaseActivity() = parentActivity


    interface CallBack {
        fun onFragmentAttached()
        fun onFragmentDetached(tag: String)
    }

    abstract fun setUp()
}