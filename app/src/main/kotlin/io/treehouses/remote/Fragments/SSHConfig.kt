package io.treehouses.remote.Fragments

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.treehouses.remote.Constants
import io.treehouses.remote.Fragments.DialogFragments.EditHostDialog
import io.treehouses.remote.Fragments.DialogFragments.SSHAllKeys
import io.treehouses.remote.Fragments.DialogFragments.SSHKeyGen
import io.treehouses.remote.R
import io.treehouses.remote.SSH.Terminal.TerminalManager
import io.treehouses.remote.SSH.beans.HostBean
import io.treehouses.remote.SSH.interfaces.OnHostStatusChangedListener
import io.treehouses.remote.SSHConsole
import io.treehouses.remote.Views.RecyclerViewClickListener
import io.treehouses.remote.adapter.ViewHolderSSHRow
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.RVButtonClick
import io.treehouses.remote.databinding.DialogSshBinding
import io.treehouses.remote.databinding.RowSshBinding
import io.treehouses.remote.utils.SaveUtils
import java.lang.Exception
import java.util.regex.Pattern


class SSHConfig : BaseFragment(), RVButtonClick, OnHostStatusChangedListener {
    private val sshPattern = Pattern.compile("^(.+)@(([0-9a-z.-]+)|(\\[[a-f:0-9]+\\]))(:(\\d+))?$", Pattern.CASE_INSENSITIVE)
    private lateinit var bind: DialogSshBinding
    private lateinit var pastHosts: List<HostBean>
    private lateinit var adapter : RecyclerView.Adapter<ViewHolderSSHRow>
    private var bound : TerminalManager? = null
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            bound = (service as TerminalManager.TerminalBinder).service
            // update our listview binder to find the service
            setUpAdapter()
            if (!bound?.hostStatusChangedListeners?.contains(this@SSHConfig)!!) {
                bound?.hostStatusChangedListeners?.add(this@SSHConfig)
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            bound?.hostStatusChangedListeners?.remove(this@SSHConfig)
            bound = null
            setUpAdapter()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogSshBinding.inflate(inflater, container, false)
        if (listener.getChatService().state == Constants.STATE_CONNECTED) {
            listener.sendMessage(getString(R.string.TREEHOUSES_NETWORKMODE_INFO))
            listener.getChatService().updateHandler(mHandler)
        }
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEnabled(false)
        addTextValidation()
        bind.connectSsh.setOnClickListener {
            var uriString = bind.sshTextInput.text.toString()
            if (!uriString.startsWith("ssh://")) uriString = "ssh://$uriString"
            val host = HostBean()
            host.setHostFromUri(Uri.parse(uriString))
            SaveUtils.updateHostList(requireContext(), host)
            Log.e("HOST URI", host.uri.toString())
            launchSSH(requireActivity(), host)
        }

        setUpAdapter()

        bind.generateKeys.setOnClickListener { SSHKeyGen().show(childFragmentManager, "GenerateKey") }

        bind.showKeys.setOnClickListener { SSHAllKeys().show(childFragmentManager, "AllKeys") }
    }

    private fun setUpAdapter() {
        pastHosts = SaveUtils.getAllHosts(requireContext()).reversed()
        if (pastHosts.isEmpty()) {
            bind.noHosts.visibility = View.VISIBLE
            bind.pastHosts.visibility = View.GONE
        }
        adapter = object : RecyclerView.Adapter<ViewHolderSSHRow>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderSSHRow {
                val holderBinding = RowSshBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolderSSHRow(holderBinding, this@SSHConfig)
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

    private fun addTextValidation() {
        bind.sshTextInput.addTextChangedListener {
            if (sshPattern.matcher(it.toString()).matches()) {
                bind.sshTextInput.error = null
                setEnabled(true)
            } else {
                bind.sshTextInput.error = "Unknown Format"
                setEnabled(false)
            }
        }
    }

    fun setEnabled(bool: Boolean) {
        bind.connectSsh.isEnabled = bool
        bind.connectSsh.isClickable = bool
    }

    private fun launchSSH(activity: FragmentActivity, host: HostBean) {
        val contents = Intent(Intent.ACTION_VIEW, host.uri)
        contents.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        contents.setClass(activity, SSHConsole::class.java)
        activity.startActivity(contents)
    }

    private fun getIP(s: String) {
        if (!s.contains("ip") || !s.startsWith("essid")) return

        val ipString = s.split(", ")[1]
        val ipAddress = ipString.substring(4)
        val hostAddress = "pi@$ipAddress"
        bind.sshTextInput.setText(hostAddress)
        Log.e("GOT IP", ipAddress)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.bindService(Intent(context, TerminalManager::class.java), connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        try {activity?.unbindService(connection)} catch (e: Exception) {Log.e("SSHConfig", e.message, e)}
    }

    override fun getMessage(msg: Message) {
        when (msg.what) {
            Constants.MESSAGE_READ -> {
                val output = msg.obj as String
                if (output.isNotEmpty()) getIP(output)
            }
        }
    }

    override fun onButtonClick(position: Int) {
        val edit = EditHostDialog()
        edit.setOnDismissListener(DialogInterface.OnDismissListener { setUpAdapter() })
        edit.arguments = Bundle().apply { putString(EditHostDialog.SELECTED_HOST_URI, pastHosts[position].uri.toString())}
        edit.show(childFragmentManager, "EditHost")
    }

    override fun onHostStatusChanged() {
        if (context != null) setUpAdapter()
    }
}