package io.treehouses.remote;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private final LayoutInflater inf;
    private ArrayList<String> groups;
    private String[][] children;

    public ExpandableListAdapter(Context context, ArrayList<String> groups, String[][] children) {
        this.groups = groups;
        this.children = children;
        inf = LayoutInflater.from(context);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return children[groupPosition].length;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return children[groupPosition][childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inf.inflate(R.layout.list_group, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.lblListHeader);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(getGroup(groupPosition).toString());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder;
        String child = getChild(groupPosition, childPosition).toString();
        String group = groups.get(groupPosition).trim();
        Log.e("TAG", "getChildView was called");

        if (child.trim().contains("Reset") || child.trim().contains("Reboot")) {
            convertView = inf.inflate(R.layout.list_item2, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.listItemStatic);
            convertView.setTag(holder);

            holder.textView.setText(child);
        } else {
            convertView = inf.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.editText = convertView.findViewById(R.id.lblListItem);
            convertView.setTag(holder);
            holder.editText.setHint(child);
        }

        if (child.trim().equals("Start Configuration")) {
            convertView = inf.inflate(R.layout.list_button, parent, false);
            buttonLayout(convertView, parent, child);
        }

        return convertView;
    }

    private void buttonLayout(View convertView, ViewGroup parent, String child) {
        ViewHolder holder = new ViewHolder();
        holder.button = convertView.findViewById(R.id.listButton);
        convertView.setTag(holder);
        holder.button.setText(child);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private class ViewHolder {
        TextInputEditText editText;
        TextView textView;
        Button button;
    }
}


