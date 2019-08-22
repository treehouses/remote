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
        myItems.add("Test");
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.profile_bridge, null);
            holder.viewCaption = (EditText) convertView.findViewById(R.id.editTextIp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
      //  Fill EditText with the value you have in data source
        holder.viewCaption.setText(myItems.get(position).toString());
        holder.viewCaption.setId(position);

//        we need to update adapter once we finish with editing
//        holder.viewCaption.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus){
//                final int position1 = v.getId();
//                final EditText Caption = (EditText) v;
//                myItems.get(position).caption = Caption.getText().toString();
//            }
//        });
        return convertView;
    }
}

class ViewHolder {
    EditText viewCaption;
}

class ListItem {
    String caption;
}
