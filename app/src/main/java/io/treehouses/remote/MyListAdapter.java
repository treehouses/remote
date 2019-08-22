package io.treehouses.remote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

import java.util.ArrayList;

public class MyListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList myItems = new ArrayList();

    public MyListAdapter(Context context) {
        mInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ListItem listItem = new ListItem();
        myItems.add(listItem.caption);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return myItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.profile_ethernet, parent, false);
            holder.viewCaption = convertView.findViewById(R.id.editTextIp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
      //  Fill EditText with the value you have in data source
        holder.viewCaption.setText("test");
        holder.viewCaption.setId(position);

//        we need to update adapter once we finish with editing
        holder.viewCaption.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus){
                final EditText Caption = (EditText) v;
                myItems.set(position, Caption.getText().toString());
            }
        });
        return convertView;
    }
}

class ViewHolder {
    EditText viewCaption;
}

class ListItem {
    String caption;
}
