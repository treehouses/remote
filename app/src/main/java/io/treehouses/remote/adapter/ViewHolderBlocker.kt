package io.treehouses.remote.adapter

import android.content.Context
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener

class ViewHolderBlocker internal constructor(v: View, context: Context?, listener: HomeInteractListener) {
    private val radioGroup: RadioGroup = v.findViewById(R.id.radioGroup)

    init {
        radioGroup.setOnCheckedChangeListener { _: RadioGroup?, i: Int ->
            when (i) {
                R.id.radioButton1 -> {
                    listener.sendMessage("treehouses blocker 0")
                    context.toast("Blocker Disabled")
                }
                R.id.radioButton2 -> {
                    listener.sendMessage("treehouses blocker 1")
                    context.toast("Blocker set to level 1")
                }
                R.id.radioButton3 -> {
                    listener.sendMessage("treehouses blocker 2")
                    context.toast("Blocker set to level 2")
                }
                R.id.radioButton4 -> {
                    listener.sendMessage("treehouses blocker 3")
                    context.toast("Blocker set to level 3")
                }
                R.id.radioButton5 -> {
                    listener.sendMessage("treehouses blocker 4")
                    context.toast("Blocker set to level 4")
                }
                R.id.radioButton6 -> {
                    listener.sendMessage("treehouses blocker max")
                    context.toast("Blocker set to maximum level")
                }
            }
        }
    }

    fun Context?.toast(s: String): Toast {
        return Toast.makeText(this, s, Toast.LENGTH_SHORT).apply { show() }
    }


}