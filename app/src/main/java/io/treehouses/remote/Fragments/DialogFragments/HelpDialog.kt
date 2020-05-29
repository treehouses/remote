package io.treehouses.remote.Fragments.DialogFragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseDialogFragment
import io.treehouses.remote.databinding.DialogHelpBinding
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.match


class HelpDialog : BaseDialogFragment() {
    lateinit var bind: DialogHelpBinding
    var jsonString = ""
    var jsonReceiving = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogHelpBinding.inflate(inflater, container, false)
        listener.chatService.updateHandler(mHandler)
        listener.sendMessage(getString(R.string.TREEHOUSES_HELP_JSON))
        jsonReceiving = true
        bind.progressBar.visibility = View.VISIBLE
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.closeButton.setOnClickListener { dismiss() }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == Constants.MESSAGE_READ) {
                val output = msg.obj as String
                if (output.isNotEmpty()) readMessage(output)

            }
        }
    }

    private fun readMessage(output: String) {
        when {
            jsonReceiving -> jsonString += output
            jsonReceiving && jsonString.trim().endsWith("\" }") -> {
                jsonReceiving = false
                bind.progressBar.visibility = View.GONE
            }
            match(output) == RESULTS.START_JSON -> {
                Log.d("JSON", output)
                jsonString = ""
                jsonReceiving = true
            }
        }

    }
}