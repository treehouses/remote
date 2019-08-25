package io.treehouses.remote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

public class MyListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private static ArrayList myItems = new ArrayList();
    private static int layout = R.layout.profile_ethernet;

    public MyListAdapter(Context context) {
        mInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//        setLayout(R.layout.profile_ethernet);

        notifyDataSetChanged();
    }

    public static void setLayout(int layout) {
        MyListAdapter.layout = layout;
    }

    public static void setMyItems(String value) {
        MyListAdapter.myItems.add(value);
    }

    public static ArrayList getMyItems() {
        return myItems;
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

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        holder = new ViewHolder();
        convertView = mInflater.inflate(layout, parent, false);
        setVariables(convertView, holder, position);
        convertView.setTag(holder);

        return convertView;
    }
    private void setVariables(View convertView, ViewHolder holder, int position) {
        switch (layout) {
            case R.layout.profile_ethernet:
                ethernet(holder, convertView, position);
                break;
            case R.layout.profile_wifi:
                wifi(holder, convertView, position);
                break;
            case R.layout.profile_hotspot:
                hotspot(holder, convertView, position);
                break;
            case R.layout.profile_bridge:
                bridge(holder, convertView, position);
                break;
        }
    }

    private void ethernet(ViewHolder holder, View convertView, int position) {
        holder.editText1 = convertView.findViewById(R.id.editTextIp);
        holder.editText2 = convertView.findViewById(R.id.editTextMask);
        holder.editText3 = convertView.findViewById(R.id.editTextGateway);
        holder.editText4 = convertView.findViewById(R.id.editTextDns);

        setTextValue(holder.editText1, "ethernet1", position);
        setTextValue(holder.editText2, "ethernet2", position);
        setTextValue(holder.editText3, "ethernet3", position);
        setTextValue(holder.editText4, "ethernet4", position);
    }

    private void wifi(ViewHolder holder, View convertView, int position) {
        holder.editText1 = convertView.findViewById(R.id.editTextSSID);
        holder.editText2 = convertView.findViewById(R.id.editTextPassword);

        setTextValue(holder.editText1, "wifi1", position);
        setTextValue(holder.editText2, "wifi2", position);
    }

    private void hotspot(ViewHolder holder, View convertView, int position) {
        holder.editText1 = convertView.findViewById(R.id.editTextSsid);
        holder.editText2 = convertView.findViewById(R.id.editTextPassword);
        holder.spinner = convertView.findViewById(R.id.spinner);

        setTextValue(holder.editText1, "hotspot1", position);
        setTextValue(holder.editText2, "hotspot2", position);


//        String value = myItems.get(position).toString();
//        holder.spinner.setSelection((value.equals("internet") ? 0 : 1));
       // holder.spinner.setId(position);
    }

    private void bridge(ViewHolder holder, View convertView, int position) {
        holder.editText1 = convertView.findViewById(R.id.editTextSsid);
        holder.editText2 = convertView.findViewById(R.id.editTextPassword);
        holder.editText3 = convertView.findViewById(R.id.editTextHostpotSsid);
        holder.editText4 = convertView.findViewById(R.id.editTextHostpotPassword);

        setTextValue(holder.editText1, "bridge1", position);
        setTextValue(holder.editText2, "bridge2", position);
        setTextValue(holder.editText3, "bridge3", position);
        setTextValue(holder.editText4, "bridge4", position);

    }

    private void setTextValue(EditText editText, String string, int position) {
        editText.setText(string);
        editText.setId(position);
    }
}

class ViewHolder {
    EditText editText1;
    EditText editText2;
    EditText editText3;
    EditText editText4;

    Spinner spinner;
}

