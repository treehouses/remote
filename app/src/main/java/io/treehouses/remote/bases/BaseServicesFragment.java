package io.treehouses.remote.bases;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import io.treehouses.remote.pojo.ServiceInfo;

public class BaseServicesFragment extends BaseFragment {
    private static final int[] MINIMUM_VERSION = {1, 14, 1};

    protected void openLocalURL(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + url));
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

    protected void showInfoDialog(String buildString) {
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

    protected boolean installedOrRunning(ServiceInfo selected) {
        return selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED || selected.serviceStatus == ServiceInfo.SERVICE_RUNNING;
    }

}
