package io.treehouses.remote.ui.services

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.adapter.ServicesListAdapter
import io.treehouses.remote.callback.ServicesListener
import io.treehouses.remote.databinding.ActivityServicesTabFragmentBinding

class ServicesOverviewFragment() : BaseServicesFragment(), OnItemClickListener {
    private var adapter: ServicesListAdapter? = null
    private var servicesListener: ServicesListener? = null
    private var used = 0
    private var total = 1
    private var bind: ActivityServicesTabFragmentBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = activity?.run { ViewModelProvider(this)[ServicesViewModel::class.java] }!!

        mChatService = listener.getChatService()
        bind = ActivityServicesTabFragmentBinding.inflate(inflater, container, false)
        return bind!!.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Tutorials.servicesOverviewTutorials(bind!!, requireActivity())
        viewModel.servicesData.observe(viewLifecycleOwner, Observer {
            if (it != null && it.isNotEmpty()) {
                adapter = ServicesListAdapter(requireContext(), it, ContextCompat.getColor(requireContext(), R.color.bg_white))
                bind!!.listView.adapter = adapter
                bind!!.listView.onItemClickListener = this
            }
        })
    }

    @JvmField
    val handlerOverview: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    val output = msg.obj as String
                    moreAction(output)
                }
                Constants.MESSAGE_WRITE -> {
                    val write_msg = String((msg.obj as ByteArray))
                    Log.d("WRITE", write_msg)
                }
                Constants.MESSAGE_STATE_CHANGE -> {
                    listener.redirectHome()
                }
            }
        }
    }

    private fun moreAction(output: String) {
        try {
            val i = output.trim { it <= ' ' }.toInt()
            if (i >= total) {
                total = i
                writeToRPI("treehouses memory used")
            } else {
                used = i
                ObjectAnimator.ofInt(bind!!.spaceLeft, "progress", (used.toFloat() / total * 100).toInt())
                        .setDuration(600)
                        .start()
            }
        } catch (ignored: NumberFormatException) {
        }
        Log.d(TAG, "moreAction: " + String.format("Used: %d / %d ", used, total) + (used.toFloat() / total * 100).toInt() + "%")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            servicesListener = parentFragment as ServicesListener?
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val selected = viewModel.servicesData.value!![position]
        if (servicesListener != null) servicesListener!!.onClick(selected)
    }

    override fun onResume() {
        super.onResume()
        writeToRPI("treehouses memory total\n")
    }

    companion object {
        private const val TAG = "ServicesTabFragment"
    }

}