package io.treehouses.remote.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

import io.treehouses.remote.R;
import io.treehouses.remote.databinding.ListRpiItemBinding;
import io.treehouses.remote.pojo.DeviceInfo;

public class RPIListAdapter extends ArrayAdapter<DeviceInfo> {
    private List<DeviceInfo> data;
    private Context context;

    private ListRpiItemBinding bind;
    public RPIListAdapter(Context context, List<DeviceInfo> data) {
        super(context, 0, data);
        this.data = data;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String deviceText = data.get(position).getDeviceName();
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            bind = ListRpiItemBinding.inflate(LayoutInflater.from(getContext()), parent, false);
        }

        bind.deviceInfo.setText(deviceText);
        bind.pairedIcon.setVisibility(View.INVISIBLE);
        if (data.get(position).isPaired()) {
            bind.pairedIcon.setVisibility(View.VISIBLE);
            if (data.get(position).isInRange()) {
                bind.pairedIcon.setColorFilter(ContextCompat.getColor(context, R.color.md_green_500));
            } else {
                bind.pairedIcon.setColorFilter(ContextCompat.getColor(context, R.color.md_grey_400));
            }
        }

        // Return the completed view to render on screen
        return bind.getRoot();
    }
}
