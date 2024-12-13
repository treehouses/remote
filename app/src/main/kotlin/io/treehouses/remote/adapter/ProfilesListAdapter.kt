package io.treehouses.remote.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.treehouses.remote.R
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.utils.SaveUtils.deleteProfile
import java.util.*

class ProfilesListAdapter(private val context: Context, private val titles: List<String>, private val data: HashMap<String, MutableList<NetworkProfile>>) : BaseExpandableListAdapter() {
    override fun getGroupCount(): Int {
        return titles.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return if (isChildEmpty(groupPosition)) {
            1
        } else data[titles[groupPosition]]!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return "YEEY"
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return data[titles[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return 0
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return 0
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        val newView: View
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        newView = layoutInflater.inflate(R.layout.list_group, null)
        val label = newView.findViewById<TextView>(R.id.lblListHeader)
        label.text = titles[groupPosition]
        return newView
    }

    private fun isChildEmpty(groupPosition: Int): Boolean {
        return data[titles[groupPosition]] == null || data[titles[groupPosition]]!!.size == 0
    }

    private fun setLabelText(label: TextView, s: String, delete: Button) {
        label.text = s
        label.setTextColor(ContextCompat.getColor(context, R.color.expandable_child_text))
        delete.visibility = View.GONE
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        val newView: View?
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        newView = layoutInflater.inflate(R.layout.row_profile, null)
        val label = newView.findViewById<TextView>(R.id.label)
        val deleteProfile = newView.findViewById<Button>(R.id.delete_profile)
        if (groupPosition == 3) {
            setLabelText(label, "Switch to Default Network", deleteProfile)
            return newView
        }
        if (isChildEmpty(groupPosition)) {
            setLabelText(label, "Please configure in the Network screen", deleteProfile)
            return newView
        }
        label.text = data[titles[groupPosition]]!![childPosition].ssid
        deleteProfile.setOnClickListener { showConfirmation(data[titles[groupPosition]]!![childPosition].ssid, groupPosition, childPosition) }
        return newView
    }

    private fun showConfirmation(name: String, groupPosition: Int, childPosition: Int) {
        val alertDialog = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
            .setTitle("Delete Profile?")
            .setMessage("Are you sure you want to delete the following Network Profile: $name")
            .setPositiveButton("Delete") { _, _ ->
                deleteProfile(context, groupPosition, childPosition)
                data[titles[groupPosition]]!!.removeAt(childPosition)
                notifyDataSetChanged()
            }.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }.create()
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}