package io.treehouses.remote.bases;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class BaseServicesFragment extends BaseFragment {
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


}
