package io.treehouses.remote.bases

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.treehouses.remote.fragments.dialogfragments.EditHostDialogFragment
import io.treehouses.remote.ssh.terminal.TerminalManager
import io.treehouses.remote.ssh.beans.HostBean
import io.treehouses.remote.ssh.interfaces.OnHostStatusChangedListener
import io.treehouses.remote.views.RecyclerViewClickListener
import io.treehouses.remote.adapter.ViewHolderSSHRow
import io.treehouses.remote.callback.RVButtonClickListener
import io.treehouses.remote.databinding.DialogSshBinding
import io.treehouses.remote.databinding.RowSshBinding
import io.treehouses.remote.utils.SaveUtils
import io.treehouses.remote.utils.logD
import java.lang.Exception
import java.util.regex.Pattern

open class BaseSSHConfig: BaseFragment(), RVButtonClickListener, OnHostStatusChangedListener {
    protected val sshPattern = Pattern.compile("^(.+)@(([0-9a-z.-]+)|(\\[[a-f:0-9]+\\]))(:(\\d+))?$", Pattern.CASE_INSENSITIVE)
    protected lateinit var bind: DialogSshBinding
    protected lateinit var pastHosts: List<HostBean>
    protected lateinit var adapter : RecyclerView.Adapter<ViewHolderSSHRow>
    protected var bound : TerminalManager? = null
    protected val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            bound = (service as TerminalManager.TerminalBinder).service
            // update our listview binder to find the service
            setUpAdapter()
            if (!bound?.hostStatusChangedListeners?.contains(this@BaseSSHConfig)!!) {
                bound?.hostStatusChangedListeners?.add(this@BaseSSHConfig)
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            bound?.hostStatusChangedListeners?.remove(this@BaseSSHConfig)
            bound = null
            setUpAdapter()
        }
    }

    protected fun setUpAdapter() {
        pastHosts = SaveUtils.getAllHosts(requireContext()).reversed()
        if (pastHosts.isEmpty()) {
            bind.noHosts.visibility = View.VISIBLE
            bind.pastHosts.visibility = View.GONE
        }
        adapter = object : RecyclerView.Adapter<ViewHolderSSHRow>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderSSHRow {
                val holderBinding = RowSshBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolderSSHRow(holderBinding, this@BaseSSHConfig)
            }

            override fun getItemCount(): Int { return pastHosts.size }

            override fun onBindViewHolder(holder: ViewHolderSSHRow, position: Int) {
                val host = pastHosts[position]
                holder.bind(host)
                if (bound?.mHostBridgeMap?.get(host)?.get() != null) holder.setConnected(true) else holder.setConnected(false)
            }
        }
        bind.pastHosts.adapter = adapter
        addItemTouchListener()
    }

    private fun addItemTouchListener() {
        val listener = RecyclerViewClickListener(requireContext(), bind.pastHosts, object : RecyclerViewClickListener.ClickListener {
            override fun onClick(view: View?, position: Int) {
                val clicked = pastHosts[position]
                bind.sshTextInput.setText(clicked.getPrettyFormat())
            }
            override fun onLongClick(view: View?, position: Int) {}
        })
        bind.pastHosts.addOnItemTouchListener(listener)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.bindService(Intent(context, TerminalManager::class.java), connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        try {activity?.unbindService(connection)} catch (e: Exception) {logD("SSHConfig $e")}
    }

    override fun onButtonClick(position: Int) {
        val edit = EditHostDialogFragment()
        edit.setOnDismissListener(DialogInterface.OnDismissListener { setUpAdapter() })
        edit.arguments = Bundle().apply { putString(EditHostDialogFragment.SELECTED_HOST_URI, pastHosts[position].uri.toString())}
        edit.show(childFragmentManager, "EditHost")
    }

    override fun onHostStatusChanged() {
        if (context != null) setUpAdapter()
    }
}