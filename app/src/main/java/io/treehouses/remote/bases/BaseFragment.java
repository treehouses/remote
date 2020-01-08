package io.treehouses.remote.bases;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.callback.HomeInteractListener;

public class BaseFragment extends Fragment {
    public BluetoothChatService mChatService = null;
    public BluetoothAdapter mBluetoothAdapter = null;

    public HomeInteractListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HomeInteractListener)
            listener = (HomeInteractListener) context;
        else
            throw new RuntimeException("Implement interface first");
    }

    public void onLoad(Handler mHandler) {
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
        checkStatusNow();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        } else {
            setupChat();
        }
    }

    public void checkStatusNow() {
    }

    public void setupChat() {
    }


}
