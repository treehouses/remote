package io.treehouses.remote.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.*
import io.treehouses.remote.Constants
import io.treehouses.remote.network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.utils.logD

class ViewHolderBlocker internal constructor(v: View, context: Context?, listener: HomeInteractListener) {
    private val blockerBar: SeekBar = v.findViewById(R.id.blockerBar)
    private val levelText: TextView = v.findViewById(R.id.levelText)
    private val blockerDesc: TextView = v.findViewById(R.id.blockerDesc)
    private val setBlocker: Button = v.findViewById(R.id.setBlocker)
    private val moveText: TextView = v.findViewById(R.id.moveText)
    private var blockerLevel:Int = 0
    private var previousLevel:Int = 0
    private var readMessage  = ""
    private val mChatService: BluetoothChatService = listener.getChatService()

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    readMessage = msg.obj as String
                    logD("readMessage = $readMessage")
                    updateSelection(readMessage)
                }
            }
        }
    }

    fun setDisplays(progress:Int) {
        when (progress) {
            0 -> levelText.text = "Blocker Disabled"
            in 1..4 -> levelText.text = "Blocker Level $progress"
            5 -> levelText.text = "Blocker Level Max"
        }

        when (progress) {
            0 -> blockerDesc.text = "Blocking Nothing"
            1 -> blockerDesc.text = "Blocking Adware + Malware"
            2 -> blockerDesc.text = "Blocking Ads + Porn"
            3 -> blockerDesc.text = "Blocking Ads + Gambling + Porn"
            4 -> blockerDesc.text = "Blocking Ads + Fake News + Gambling + Porn"
            5 -> blockerDesc.text = "Blocking Ads + Fake News + Gambling + Porn + Social"
        }
    }


    init {
        mChatService.updateHandler(mHandler)
        listener.sendMessage(context!!.resources.getString(R.string.TREEHOUSES_BLOCKER_CHECK))
        blockerBar.max = 5
        setBlocker.visibility = View.GONE
        moveText.visibility = View.VISIBLE
        blockerBar.isEnabled = false

        blockerBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                blockerLevel = progress
                if(blockerLevel == previousLevel) {
                    setBlocker.visibility = View.GONE
                    moveText.visibility = View.VISIBLE
                }
                else {
                    setBlocker.visibility = View.VISIBLE
                    moveText.visibility = View.GONE
                }

                setDisplays(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })


        fun setBlocker(level:Int) {
            fun sendCommand(level:String) {
                listener.sendMessage(context.resources.getString(R.string.TREEHOUSES_BLOCKER, level))
            }

            when (level) {
                in 0..4 -> sendCommand(level.toString())
                5 -> sendCommand("max")
            }
        }

        setBlocker.setOnClickListener {
            previousLevel = blockerLevel
            setBlocker.visibility = View.GONE
            moveText.visibility = View.VISIBLE
            blockerBar.isEnabled = false
            setBlocker(blockerLevel)
            when (blockerLevel) {
                0 -> context.toast("Blocker Disabled")
                1 -> context.toast("Blocker set to level 1")
                2 -> context.toast("Blocker set to level 2")
                3 -> context.toast("Blocker set to level 3")
                4 -> context.toast("Blocker set to level 4")
                5 -> context.toast("Blocker set to maximum level")
            }
        }
    }

    fun updateSelection(readMessage:String){
        fun update(level:Int){
            blockerLevel = level
            previousLevel = blockerLevel
            blockerBar.progress = blockerLevel
            blockerBar.isEnabled = true
            setDisplays(level)
        }
        when {
            readMessage.contains("blocker 0") -> update(0)
            readMessage.contains("blocker 1") -> update(1)
            readMessage.contains("blocker 2") -> update(2)
            readMessage.contains("blocker 3") -> update(3)
            readMessage.contains("blocker 4") -> update(4)
            readMessage.contains("blocker X") -> update(5)
        }

    }

    companion object {
        private const val TAG = "ViewHolderBlocker"
    }

    fun Context?.toast(s: String): Toast {
        return Toast.makeText(this, s, Toast.LENGTH_SHORT).apply { show() }
    }


}