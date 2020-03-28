package io.treehouses.remote.bases;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import io.treehouses.remote.pojo.ServiceInfo;

public class BaseServicesFragment extends BaseFragment {
    private static final int[] MINIMUM_VERSION = {1, 14, 1};

    protected void openLocalURL(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + url));

        Log.d("OPENING: ", "http://" + url + "||");
        String title = "Select a browser";
        Intent chooser = Intent.createChooser(intent, title);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) startActivity(chooser);
    }

    protected void openTorURL (String url) {
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

    protected int getQuoteCount(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\"') count++;
        }
        return count;
    }

    protected boolean checkVersion(int[] versionIntNumber) {
        if (versionIntNumber[0] > MINIMUM_VERSION[0]) return true;
        if (versionIntNumber[0] == MINIMUM_VERSION[0] && versionIntNumber[1] > MINIMUM_VERSION[1]) return true;
        return (versionIntNumber[0] == MINIMUM_VERSION[0]) && (versionIntNumber[1] == MINIMUM_VERSION[1]) && (versionIntNumber[2] >= MINIMUM_VERSION[2]);
    }
    protected void writeToRPI(String ping) {
        mChatService.write(ping.getBytes());
    }

    protected void performService(String action, String command, String name) {
        Log.d("SERVICES", action + " " + name);
        Toast.makeText(getContext(), name + " " + action, Toast.LENGTH_LONG).show();
        writeToRPI(command);
    }

    protected void showDeleteDialog(ServiceInfo selected) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Delete " + selected.name + "?")
                .setMessage("Are you sure you would like to delete this service? All of its data will be lost and the service must be reinstalled.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performService("Uninstalling", "treehouses services " + selected.name + " cleanup\n", selected.name);
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

    protected boolean installedOrRunning(ServiceInfo selected) {
        return selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED || selected.serviceStatus == ServiceInfo.SERVICE_RUNNING;
    }

    protected void updateServiceList(String[] stringList, int identifier, ArrayList<ServiceInfo> services) {
        for (String name : stringList) {
            int a = inServiceList(name, services);
            if (a >= 0 ) services.get(a).serviceStatus = identifier;
            else if (name.trim().length() > 0) services.add(new ServiceInfo(name, identifier));
        }

        if (identifier == ServiceInfo.SERVICE_RUNNING) formatList(services);
    }

    private void formatList(ArrayList<ServiceInfo> services) {
        if (inServiceList("Installed", services) == -1) services.add(0, new ServiceInfo("Installed", ServiceInfo.SERVICE_HEADER_INSTALLED));
        if (inServiceList("Available", services) == -1) services.add(0, new ServiceInfo("Available",ServiceInfo.SERVICE_HEADER_AVAILABLE));
        Collections.sort(services);
    }

    protected int inServiceList(String name, ArrayList<ServiceInfo> services) {
        for (int i = 0; i < services.size(); i++) {
            if (services.get(i).name.equals(name)) return i;
        }
        return -1;
    }

    protected boolean isVersionNumber(String s, int[] versionNumber) {
        if (!s.contains(".")) return false;
        String[] parts = s.split("[.]");
        int[] intParts = new int[3];
        if (parts.length != 3) return false;
        for (int i = 0; i < parts.length; i++) {
            try {
                intParts[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        System.arraycopy(intParts,0, versionNumber, 0, 3);
        return true;
    }
    protected int performAction(String output, TextView text, ProgressBar pbar, ArrayList<ServiceInfo> services, int[] versionIntNumber, BaseAdapter adapter) {
        int i = -1;
        if (output.startsWith("Usage:")) {
            text.setVisibility(View.VISIBLE);
            text.setText("Feature not available please upgrade cli version.");
            pbar.setVisibility(View.GONE);
            i = 0;
        } else if (isVersionNumber(output, versionIntNumber)) {
            writeToRPI("treehouses remote services available\n");
            //text.setText(output);
            i = 1;
        } else if (output.contains("Available:")) {
            pbar.setVisibility(View.VISIBLE);
            updateServiceList(output.substring(output.indexOf(":") + 2).split(" "), ServiceInfo.SERVICE_AVAILABLE, services);
            writeToRPI("treehouses remote services installed\n");
            i = 2;
        } else if (output.contains("Installed:")) {
            updateServiceList(output.substring(output.indexOf(":") + 2).split(" "), ServiceInfo.SERVICE_INSTALLED, services);
            writeToRPI("treehouses remote services running\n");
            i = 3;
        } else if (output.contains("Running:")) {
            updateServiceList(output.substring(output.indexOf(":") + 2).split(" "), ServiceInfo.SERVICE_RUNNING, services);
            adapter.notifyDataSetChanged();
            pbar.setVisibility(View.GONE);
            i = 4;
        }
        return i;
    }

    protected boolean containsXML(String output) {
        return output.contains("xml") || output.contains("xmlns");
    }

}
