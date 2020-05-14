package io.treehouses.remote.Fragments

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
import android.widget.ListView
import android.widget.ProgressBar
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.adapter.ServicesListAdapter
import io.treehouses.remote.bases.BaseServicesFragment
import io.treehouses.remote.callback.ServicesListener
import io.treehouses.remote.pojo.ServiceInfo
import java.util.*

class ServicesTabFragment(var services: ArrayList<ServiceInfo>) : BaseServicesFragment(), OnItemClickListener {
    private var view: View? = null
    private var adapter: ServicesListAdapter? = null
    private var listView: ListView? = null
    private var servicesListener: ServicesListener? = null
    private var memoryMeter: ProgressBar? = null
    private var used = 0
    private var total = 1
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mChatService = listener.chatService!!
        view = inflater.inflate(R.layout.activity_services_tab_fragment, container, false)
        memoryMeter = view.findViewById(R.id.space_left)
        listView = view.findViewById(R.id.listView)
        adapter = ServicesListAdapter(activity!!, services, resources.getColor(R.color.bg_white))
        listView.setAdapter(adapter)
        listView.setOnItemClickListener(this)
        return view
    }

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
                ObjectAnimator.ofInt(memoryMeter, "progress", (used.toFloat() / total * 100).toInt())
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
        val selected = services[position]
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