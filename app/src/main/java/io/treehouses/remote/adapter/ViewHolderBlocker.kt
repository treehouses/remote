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
                    Toast.makeText(context, "Blocker Disabled", Toast.LENGTH_SHORT).show()
                }
                R.id.radioButton2 -> {
                    listener.sendMessage("treehouses blocker 1")
                    Toast.makeText(context, "Blocker set to level 1", Toast.LENGTH_SHORT).show()
                }
                R.id.radioButton3 -> {
                    listener.sendMessage("treehouses blocker 2")
                    Toast.makeText(context, "Blocker set to level 2", Toast.LENGTH_SHORT).show()
                }
                R.id.radioButton4 -> {
                    listener.sendMessage("treehouses blocker 3")
                    Toast.makeText(context, "Blocker set to level 3", Toast.LENGTH_SHORT).show()
                }
                R.id.radioButton5 -> {
                    listener.sendMessage("treehouses blocker 4")
                    Toast.makeText(context, "Blocker set to level 4", Toast.LENGTH_SHORT).show()
                }
                R.id.radioButton6 -> {
                    listener.sendMessage("treehouses blocker max")
                    Toast.makeText(context, "Blocker set to maximum level", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}