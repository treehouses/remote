package io.treehouses.remote.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.treehouses.remote.Fragments.DialogFragments.RPIDialogFragment;
import io.treehouses.remote.R;

public class RPIListAdapter extends ArrayAdapter<String[]> {
    private List<String[]> data;
    private Context context;
    public RPIListAdapter(Context context, List<String[]> data) {
        super(context, 0, data);
        this.data = data;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        TextView text;
        String deviceText = data.get(position)[0];
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_rpi_item, parent, false);
        }

        text = convertView.findViewById(R.id.device_info);
        text.setText(deviceText);
        if (data.get(position)[1].equals(RPIDialogFragment.BONDED_TAG)) {
            text.setTextColor(context.getResources().getColor(R.color.md_green_500));
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
