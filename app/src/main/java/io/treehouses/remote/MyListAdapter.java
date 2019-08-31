package io.treehouses.remote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import static io.treehouses.remote.ButtonConfiguration.saveNetwork;

public class MyListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private static ArrayList<String> list = new ArrayList<>();
    private static int layout = R.layout.profile_ethernet;
    private Context context;

    public MyListAdapter(Context context) {
        mInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        notifyDataSetChanged();
        this.context = context;
    }

    public static void setLayout(int layout) {
        MyListAdapter.layout = layout;
    }

    public static void setList(String value) {
        MyListAdapter.list.add(value);
    }

    public static ArrayList<String> getList() {
        return list;
    }

    @Override
    public int getCount() { return list.size(); }

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
            case R.layout.profile_tether:
                tether(holder, convertView, position);
                break;
        }
    }

    private void ethernet(ViewHolder h, View convertView, int position) {
        h.editText1 = convertView.findViewById(R.id.editTextIp);
        h.editText2 = convertView.findViewById(R.id.editTextMask);
        h.editText3 = convertView.findViewById(R.id.editTextGateway);
        h.editText4 = convertView.findViewById(R.id.editTextDns);

        setTextValue(h.editText1, "ethernet1", position);
        setTextValue(h.editText2, "ethernet2", position);
        setTextValue(h.editText3, "ethernet3", position);
        setTextValue(h.editText4, "ethernet4", position);
    }

    private void wifi(ViewHolder h, View convertView, int position) {
        h.editText1 = convertView.findViewById(R.id.editTextSSID);
        h.editText2 = convertView.findViewById(R.id.editTextPassword);

        setTextValue(h.editText1, "wifi1", position);
        setTextValue(h.editText2, "wifi2", position);
    }

    private void hotspot(ViewHolder h, View convertView, int position) {
        h.editText1 = convertView.findViewById(R.id.editTextSsid);
        h.editText2 = convertView.findViewById(R.id.editTextPassword);
        h.spinner = convertView.findViewById(R.id.spinner);

        setTextValue(h.editText1, "hotspot1", position);
        setTextValue(h.editText2, "hotspot2", position);


//        String value = myItems.get(position).toString();
//        holder.spinner.setSelection((value.equals("internet") ? 0 : 1));
       // holder.spinner.setId(position);
    }

    private void bridge(ViewHolder h, View convertView, int position) {
        h.editText1 = convertView.findViewById(R.id.editTextSsid);
        h.editText2 = convertView.findViewById(R.id.editTextPassword);
        h.editText3 = convertView.findViewById(R.id.editTextHostpotSsid);
        h.editText4 = convertView.findViewById(R.id.editTextHostpotPassword);

        setTextValue(h.editText1, MainApplication.getSharedPreferences().getString("bridgeSsid", ""), position);
        setTextValue(h.editText2, MainApplication.getSharedPreferences().getString("bridgeSsidPassword", ""), position);
        setTextValue(h.editText3, MainApplication.getSharedPreferences().getString("bridgeHotspotSsid", ""), position);
        setTextValue(h.editText4, MainApplication.getSharedPreferences().getString("bridgeHotspotPassword", ""), position);

        Button button = convertView.findViewById(R.id.btn_start_config);
        button.setText(R.string.select);
        button.setOnClickListener(v -> {
            saveNetwork("bridgeSsid", h.editText1.getText().toString(), "bridgeSsidPassword", h.editText2.getText().toString(), "bridgeHotspotSsid", h.editText3.getText().toString(), "bridgeHotspotPassword", h.editText4.getText().toString());
            Toast.makeText(context, "Profile Saved", Toast.LENGTH_LONG).show();
        });
    }

    private void tether(ViewHolder h, View convertView, int position) {
        h.editText1 = convertView.findViewById(R.id.editTextSsid);
        h.editText2 = convertView.findViewById(R.id.editTextPassword);

        setTextValue(h.editText1, "tether1", position);
        setTextValue(h.editText2, "tether2", position);
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

