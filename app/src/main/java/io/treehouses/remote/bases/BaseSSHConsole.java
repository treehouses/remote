package io.treehouses.remote.bases;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import io.treehouses.remote.R;
import io.treehouses.remote.SSH.PromptHelper;
import io.treehouses.remote.SSH.Terminal.TerminalBridge;
import io.treehouses.remote.SSH.Terminal.TerminalManager;
import io.treehouses.remote.SSH.Terminal.TerminalView;
import io.treehouses.remote.SSH.Terminal.TerminalViewPager;
import io.treehouses.remote.SSH.beans.HostBean;

public class BaseSSHConsole extends BaseFragment {
//    protected TerminalManager bound = null;
//    protected TerminalPagerAdapter adapter;
//    protected RelativeLayout stringPromptGroup;
//    protected EditText stringPrompt;
//    private RelativeLayout booleanPromptGroup;
//    private TextView booleanPrompt;
//    private boolean keyboardAlwaysVisible = false;
//    private Button booleanYes;
//    private LinearLayout keyboardGroup;
//    private Runnable keyboardGroupHider;
//    protected TerminalViewPager pager = null;
//    protected TabLayout tabs = null;
//    protected Toolbar toolbar = null;
//    private Handler handler = new Handler();
//
//    @Nullable private ActionBar actionBar;
//    private boolean inActionBarMenu = false;
//    private boolean titleBarHide;
//
//    private Animation fade_out_delayed;
//
//
//
//    private TextView stringPromptInstructions;
////    protected TerminalPagerAdapter adapter = null;
//    protected LayoutInflater inflater = null;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        fade_out_delayed = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_delayed);
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }
//
//    protected void hideAllPrompts() {
//        stringPromptGroup.setVisibility(View.GONE);
//        booleanPromptGroup.setVisibility(View.GONE);
//    }
//    protected void updatePromptVisible() {
//        // check if our currently-visible terminalbridge is requesting any prompt services
//        TerminalView view = adapter.getCurrentTerminalView();
//
//        // Hide all the prompts in case a prompt request was canceled
//        hideAllPrompts();
//
//        if (view == null) {
//            // we dont have an active view, so hide any prompts
//            return;
//        }
//
//        PromptHelper prompt = view.bridge.promptHelper;
//        if (String.class.equals(prompt.promptRequested)) {
//            hideEmulatedKeys();
//            stringPromptGroup.setVisibility(View.VISIBLE);
//
//            String instructions = prompt.promptInstructions;
//            if (instructions != null && instructions.length() > 0) {
//                stringPromptInstructions.setVisibility(View.VISIBLE);
//                stringPromptInstructions.setText(instructions);
//            } else
//                stringPromptInstructions.setVisibility(View.GONE);
//            stringPrompt.setText("");
//            stringPrompt.setHint(prompt.promptHint);
//            stringPrompt.requestFocus();
//
//        } else if (Boolean.class.equals(prompt.promptRequested)) {
//            hideEmulatedKeys();
//            booleanPromptGroup.setVisibility(View.VISIBLE);
//            booleanPrompt.setText(prompt.promptHint);
//            booleanYes.requestFocus();
//
//        } else {
//            hideAllPrompts();
//            view.requestFocus();
//        }
//    }
//    private void hideEmulatedKeys() {
//        if (!keyboardAlwaysVisible) {
//            if (keyboardGroupHider != null)
//                handler.removeCallbacks(keyboardGroupHider);
//            keyboardGroup.setVisibility(View.GONE);
//        }
//        hideActionBarIfRequested();
//    }
//    private void hideActionBarIfRequested() {
//        if (titleBarHide && actionBar != null) {
//            actionBar.hide();
//        }
//    }
//    protected Handler promptHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            // someone below us requested to display a prompt
//            updatePromptVisible();
//        }
//    };
//
//    public class TerminalPagerAdapter extends PagerAdapter {
//        @Override
//        public int getCount() {
//            if (bound != null) {
//                return bound.bridges.size();
//            } else {
//                return 0;
//            }
//        }
//
//        @Override
//        public Object instantiateItem(ViewGroup container, int position) {
//            if (bound == null || bound.bridges.size() <= position) {
//                Log.w("TAG", "Activity not bound when creating TerminalView.");
//            }
//            TerminalBridge bridge = bound.bridges.get(position);
//            bridge.promptHelper.setHandler(promptHandler);
//
//            // inflate each terminal view
//            RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.item_terminal, container, false);
//
//            // set the terminal name overlay text
//            TextView terminalNameOverlay = view.findViewById(R.id.terminal_name_overlay);
//            terminalNameOverlay.setText(bridge.host.getNickname());
//
//            // and add our terminal view control, using index to place behind overlay
//            final TerminalView terminal = new TerminalView(container.getContext(), bridge, pager);
//            terminal.setId(R.id.terminal_view);
//            view.addView(terminal, 0);
//
//            // Tag the view with its bridge so it can be retrieved later.
//            view.setTag(bridge);
//
//            container.addView(view);
//            terminalNameOverlay.startAnimation(fade_out_delayed);
//            return view;
//        }
//
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            final View view = (View) object;
//
//            container.removeView(view);
//        }
//
//        @Override
//        public int getItemPosition(Object object) {
//            if (bound == null) {
//                return POSITION_NONE;
//            }
//
//            View view = (View) object;
//            TerminalView terminal = view.findViewById(R.id.terminal_view);
//            HostBean host = terminal.bridge.host;
//
//            int itemIndex = POSITION_NONE;
//            int i = 0;
//            for (TerminalBridge bridge : bound.bridges) {
//                if (bridge.host.equals(host)) {
//                    itemIndex = i;
//                    break;
//                }
//                i++;
//            }
//            return itemIndex;
//        }
//
//        public TerminalBridge getBridgeAtPosition(int position) {
//            if (bound == null) {
//                return null;
//            }
//
//            ArrayList<TerminalBridge> bridges = bound.bridges;
//            if (position < 0 || position >= bridges.size()) {
//                return null;
//            }
//            return bridges.get(position);
//        }
//
//        @Override
//        public void notifyDataSetChanged() {
//            super.notifyDataSetChanged();
//            if (tabs != null) {
//                toolbar.setVisibility(this.getCount() > 1 ? View.VISIBLE : View.GONE);
//                tabs.setTabsFromPagerAdapter(this);
//            }
//        }
//
//        @Override
//        public boolean isViewFromObject(View view, Object object) {
//            return view == object;
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            TerminalBridge bridge = getBridgeAtPosition(position);
//            if (bridge == null) {
//                return "???";
//            }
//            return bridge.host.getNickname();
//        }
//
//        public TerminalView getCurrentTerminalView() {
//            View currentView = pager.findViewWithTag(getBridgeAtPosition(pager.getCurrentItem()));
//            if (currentView == null) {
//                return null;
//            }
//            return (TerminalView) currentView.findViewById(R.id.terminal_view);
//        }
//    }
}
