package io.treehouses.remote.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
        String deviceText = data.get(position)[0];
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_rpi_item, parent, false);
        }

        TextView text = convertView.findViewById(R.id.device_info);
        ImageView pairedImage = convertView.findViewById(R.id.paired_icon);

        text.setText(deviceText);
        pairedImage.setVisibility(View.INVISIBLE);
        if (data.get(position)[1].equals(RPIDialogFragment.BONDED_TAG)) {
            pairedImage.setVisibility(View.VISIBLE);
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
