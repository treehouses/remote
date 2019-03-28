package io.treehouses.remote.callback;

import android.os.Handler;

import androidx.fragment.app.Fragment;
import io.treehouses.remote.Network.BluetoothChatService;

public interface HomeInteractListener {
    void sendMessage(String s);
    void openCallFragment(Fragment f);
    void updateHandler(Handler handler);
    BluetoothChatService getChatService();
    void setChatService(BluetoothChatService service);
}
