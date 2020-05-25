package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.tabs.TabLayout;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.SSH.PromptHelper;
import io.treehouses.remote.SSH.Terminal.TerminalBridge;
import io.treehouses.remote.SSH.Terminal.TerminalManager;
import io.treehouses.remote.SSH.Terminal.TerminalView;
import io.treehouses.remote.SSH.Terminal.TerminalViewPager;
import io.treehouses.remote.SSH.interfaces.BridgeDisconnectedListener;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.bases.BaseSSHConsole;
import io.treehouses.remote.databinding.ActivitySshConsoleBinding;

public class SSHConsole extends BaseSSHConsole implements BridgeDisconnectedListener {
    private ActivitySshConsoleBinding bind;

    private static final int KEYBOARD_DISPLAY_TIME = 3000;
    private static final int KEYBOARD_REPEAT_INITIAL = 500;
    private static final int KEYBOARD_REPEAT = 100;

    @Nullable
    protected TerminalManager bound = null;
//    protected TerminalPagerAdapter adapter = null;
    protected LayoutInflater inflater = null;

    private SharedPreferences prefs = null;

    // determines whether or not menuitem accelerators are bound
    // otherwise they collide with an external keyboard's CTRL-char
    private boolean hardKeyboard = false;

    protected Uri requested;

    protected ClipboardManager clipboard;

    private LinearLayout keyboardGroup;
    private Runnable keyboardGroupHider;

    private TextView empty;

    private Animation fade_out_delayed;

    private Animation keyboard_fade_in, keyboard_fade_out;

    private MenuItem disconnect, copy, paste, resize, urlscan;

    private boolean forcedOrientation;

    private Handler handler = new Handler();

    private View contentView;

    private ImageView mKeyboardButton;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            bound = ((TerminalManager.TerminalBinder) service).getService();

            // let manager know about our event handling services
            bound.disconnectListener = SSHConsole.this;
            bound.setResizeAllowed(true);

            final String requestedNickname = (requested != null) ? requested.getFragment() : null;
            TerminalBridge requestedBridge = bound.getConnectedBridge(requestedNickname);

            // If we didn't find the requested connection, try opening it
            if (requestedNickname != null && requestedBridge == null) {
                try {
                    Log.d("TAG", String.format("We couldnt find an existing bridge with URI=%s (nickname=%s), so creating one now", requested.toString(), requestedNickname));
                    requestedBridge = bound.openConnection(requested);
                } catch (Exception e) {
                    Log.e("TAG", "Problem while trying to create new requested bridge from URI", e);
                }
            }

            // create views for all bridges on this service
            adapter.notifyDataSetChanged();
            final int requestedIndex = bound.getBridges().indexOf(requestedBridge);

            if (requestedBridge != null)
                requestedBridge.promptHelper.setHandler(promptHandler);


            if (requestedIndex != -1) {
                pager.post(() -> setDisplayedTerminal(requestedIndex));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            bound = null;
            adapter.notifyDataSetChanged();
            updateEmptyVisible();
        }
    };


    private void setDisplayedTerminal(int requestedIndex) {
        pager.setCurrentItem(requestedIndex);
        // set activity title
        onTerminalChanged();
    }

    private void onTerminalChanged() {
        View terminalNameOverlay = findCurrentView(R.id.terminal_name_overlay);
        if (terminalNameOverlay != null)
            terminalNameOverlay.startAnimation(fade_out_delayed);
        updateDefault();
        updatePromptVisible();
        ActivityCompat.invalidateOptionsMenu(requireActivity());
    }
    protected View findCurrentView(int id) {
        View view = pager.findViewWithTag(adapter.getBridgeAtPosition(pager.getCurrentItem()));
        if (view == null) {
            return null;
        }
        return view.findViewById(id);
    }

    private void updateDefault() {
        // update the current default terminal
        TerminalView view = adapter.getCurrentTerminalView();
        if (view == null || bound == null) {
            return;
        }
        bound.defaultBridge = view.bridge;
    }

    protected void updateEmptyVisible() {
        // update visibility of empty status message
        empty.setVisibility((pager.getChildCount() == 0) ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onStart() {
        super.onStart();

        // connect with manager service to find all bridges
        // when connected it will insert all views
        requireActivity().bindService(new Intent(getActivity(), TerminalManager.class), connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();

        requireActivity().unbindService(connection);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bind = ActivitySshConsoleBinding.inflate(inflater, container, false);
        return bind.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onLoad(mHandler);
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_READ) {
                String readMessage = (String) msg.obj;
                Log.d("TAG", "readMessage = " + readMessage);
//                performAction(readMessage);
            }
        }
    };

    @Override
    public void onDisconnected(TerminalBridge bridge) {

    }
}
