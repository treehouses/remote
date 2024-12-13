package io.treehouses.remote.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import io.treehouses.remote.R
import io.treehouses.remote.pojo.CommandListItem
import java.util.*

class CommandListAdapter(private val context: Context, private val expandableListTitle: List<String>, private val expandableListDetail: HashMap<String, List<CommandListItem>>) : BaseExpandableListAdapter() {
    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return if (expandedListPosition < expandableListDetail[expandableListTitle[listPosition]]!!.size) {
            expandableListDetail[expandableListTitle[listPosition]]!![expandedListPosition].getTitle()
        } else {
            "Add"
        }
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(listPosition: Int, expandedListPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        val newView: View?
        val expandedListText = getChild(listPosition, expandedListPosition) as String
        if (expandedListPosition == expandableListDetail[expandableListTitle[listPosition]]!!.size) {
            newView = getConvertView(R.layout.list_add)
        } else {
            newView = getConvertView(R.layout.list_item)
            val expandedListTextView = newView.findViewById<TextView>(R.id.expandedListItem)
            expandedListTextView.text = expandedListText
        }
        return newView
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return if (listPosition == 0) {
            expandableListDetail[expandableListTitle[listPosition]]!!.size + 1
        } else {
            expandableListDetail[expandableListTitle[listPosition]]!!.size
        }
    }

    override fun getGroup(listPosition: Int): Any {
        return expandableListTitle[listPosition]
    }

    override fun getGroupCount(): Int {
        return expandableListTitle.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(listPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        var newView = convertView
        val listTitle = getGroup(listPosition) as String
        if (newView == null) {
            newView = getConvertView(R.layout.list_group)
        }
        val listTitleTextView = newView.findViewById<View>(R.id.lblListHeader) as TextView
        listTitleTextView.text = listTitle
        return newView
    }

    private fun getConvertView(layout_id: Int): View {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return layoutInflater.inflate(layout_id, null)
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}