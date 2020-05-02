package io.treehouses.remote.bases;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.callback.HomeInteractListener;

public class BaseFragment extends Fragment {
    public BluetoothChatService mChatService;

    public HomeInteractListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeInteractListener)
            listener = (HomeInteractListener) context;
        else
            throw new RuntimeException("Implement interface first");

        mChatService = listener.getChatService();

    }

    public void setupChat() {

    }

    protected void onLoad(Handler mHandler) {
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);
        // If the adapter is null, then Bluetooth is not supported
        if (!mChatService.isBluetoothSupported()) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
        if (!mChatService.isBluetoothEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        }
    }


}
