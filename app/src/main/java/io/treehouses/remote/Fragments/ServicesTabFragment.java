package io.treehouses.remote.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.ServicesListAdapter;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesTabFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private View view;
    private ProgressBar progressBar;
    private ArrayList<ServiceInfo> services;
    ServicesListAdapter adapter;
    private TextView tvMessage;
    private boolean received = false;
    private boolean infoClicked;
    private int quoteCount;
    private String buildString;

    public ServicesTabFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);

        writeToRPI("treehouses remote services available\n");

        view = inflater.inflate(R.layout.activity_services_tab_fragment, container, false);
        progressBar = view.findViewById(R.id.progress_services);
        tvMessage = view.findViewById(R.id.tv_message);
        progressBar.setVisibility(View.VISIBLE);
        services = new ArrayList<ServiceInfo>();

        ListView listView = view.findViewById(R.id.listView);
        adapter = new ServicesListAdapter(getActivity(), services);
        listView.setAdapter(adapter);

        listView.setItemsCanFocus(false);

        listView.setOnItemClickListener(this);

        return view;
    }

    private void writeToRPI(String ping) {
        mChatService.write(ping.getBytes());
    }

    private void performAction(String output) {
        if (output.startsWith("Usage:")) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Feature not available please upgrade cli version.");
            progressBar.setVisibility(View.GONE);
        } else if (output.contains("Available:")) {
            //Read
            tvMessage.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            updateServiceList(output.substring(output.indexOf(":") + 2).split(" "), ServiceInfo.SERVICE_AVAILABLE);
            writeToRPI("treehouses remote services installed\n");
        }

        else {
            moreActions(output);
        }
    }

    private void openLocalURL(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + url));
        String title = "Select a browser";
        Intent chooser = Intent.createChooser(intent, title);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) startActivity(chooser);
    }

    private void openTorURL (String url) {
        Intent intent = getContext().getPackageManager().getLaunchIntentForPackage("org.torproject.torbrowser");
        if (intent != null) {
            intent.setData(Uri.parse("http://" + url));
            startActivity(intent);//null pointer check in case package name was not found
        }
        else {
            final String s = "Please install Tor Browser from: \n\n https://play.google.com/store/apps/details?id=org.torproject.torbrowser";
            final SpannableString spannableString = new SpannableString(s);
            Linkify.addLinks(spannableString, Linkify.ALL);
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).setTitle("Tor Browser Not Found").setMessage(spannableString).create();
            alertDialog.show();
            TextView alertTextView = (TextView) alertDialog.findViewById(android.R.id.message);
            alertTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    String output = (String) msg.obj;
                    performAction(output);
                    break;
                case Constants.MESSAGE_WRITE:
                    String write_msg = new String((byte[]) msg.obj);
                    Log.d("WRITE", write_msg);
                    break;

            }
        }
    };

    private void increaseQuoteCount(String output) {
        quoteCount += getQuoteCount(output);
        buildString += output;
        if (output.startsWith("https://")) {
            buildString += "\n\n";
        }
        if (quoteCount >= 2) {
            showInfoDialog();
        }
    }

    private void moreActions(String output) {
        if (output.contains(".") && output.contains(":") && output.length() < 20 && !received) {
            received = true;
            openLocalURL(output);
        }
        else if (output.contains(".onion") && ! received) {
            received = true;
            openTorURL(output);
        }
        else if (output.contains("Installed:")) {
            updateServiceList(output.substring(output.indexOf(":") + 2).split(" "), ServiceInfo.SERVICE_INSTALLED);
            writeToRPI("treehouses remote services running\n");
        } else if (output.contains("Running:")) {
            updateServiceList(output.substring(output.indexOf(":") + 2).split(" "), ServiceInfo.SERVICE_RUNNING);
        }
        else if (infoClicked) {
            increaseQuoteCount(output);
        }
    }

    private void showInfoDialog() {
        final SpannableString s = new SpannableString(buildString);
        Linkify.addLinks(s, Linkify.ALL);
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).setTitle("Info").setMessage(s)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
        TextView alertTextView = (TextView) alertDialog.findViewById(android.R.id.message);
        alertTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public int getQuoteCount(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\"') count++;
        }
        return count;
    }

    private void updateServiceList(String[] stringList, int identifier) {
        for (String name : stringList) {
            int a = inServiceList(name);
            if (a >= 0) services.get(a).serviceStatus = identifier;
            else if (name.trim().length() > 0) services.add(new ServiceInfo(name, identifier));
        }
        adapter.notifyDataSetChanged();
    }

    private int inServiceList(String name) {
        for (int i = 0; i < services.size(); i++) {
            if (services.get(i).name.equals(name)) return i;
        }
        return -1;
    }

    private void performService(String action, String command, String name) {
        Log.d("SERVICES", action + " " + name);
        Toast.makeText(getContext(), name + " " + action, Toast.LENGTH_LONG).show();
        writeToRPI(command);
    }

    private void onClickInstall(ServiceInfo selected) {
        if (selected.serviceStatus == ServiceInfo.SERVICE_AVAILABLE) {
            performService("Installing", "treehouses services " + selected.name + " up\n", selected.name);
            writeToRPI("treehouses remote services available\n");
        }
        else if (selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED || selected.serviceStatus == ServiceInfo.SERVICE_RUNNING) {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Delete " + selected.name + "?")
                    .setMessage("Are you sure you would like to delete this service? All of its data will be lost and the service must be reinstalled.")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            performService("Uninstalling", "treehouses services " + selected.name + " down\n", selected.name);
                            writeToRPI("treehouses remote services available\n");
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            alertDialog.show();
        }
    }

    private void onClickStart(ServiceInfo selected) {
        if (selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED) {
            performService("Starting", "treehouses services " + selected.name + " start\n", selected.name);
        }
        else if (selected.serviceStatus == ServiceInfo.SERVICE_RUNNING) {
            performService("Stopping", "treehouses services " + selected.name + " stop\n", selected.name);
        }
    }

    private void onClickRestart(ServiceInfo selected) {
        if (selected.serviceStatus != ServiceInfo.SERVICE_AVAILABLE) {
            performService("Restarting", "treehouses services " + selected.name + " restart\n", selected.name);
        }
    }

    private void onClickLink(ServiceInfo selected) {
        //reqUrls();
        View view = getLayoutInflater().inflate(R.layout.dialog_choose_url, null);
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle("Select URL type")
                .create();

        setOnClick(view, R.id.local_button, "treehouses services " + selected.name + " url local \n", alertDialog);
        setOnClick(view, R.id.tor_button, "treehouses services " + selected.name + " url tor \n", alertDialog);

        alertDialog.show();
        received = false;
    }

    private void setOnClick(View v, int id, String command, AlertDialog alertDialog) {
        v.findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToRPI(command);
                alertDialog.dismiss();
            }
        });
    }


    private void onClickInfo(ServiceInfo selected) {
        writeToRPI("treehouses services " + selected.name + " info");
        infoClicked = true;
        quoteCount = 0;
        buildString = "";
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ServiceInfo selected = services.get(position);
        infoClicked = false;
        switch (view.getId()) {
            case R.id.start_service:
                onClickStart(selected);
                writeToRPI("treehouses remote services available\n");
                break;
            case R.id.install_service:
                onClickInstall(selected);
                break;

            case R.id.restart_service:
                onClickRestart(selected);
                writeToRPI("treehouses remote services available\n");
                break;

            case R.id.service_info:
                onClickInfo(selected);
                break;

            case R.id.link_button:
                onClickLink(selected);
                break;
        }
    }


}