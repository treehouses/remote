package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.util.ArrayList;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseServicesFragment;
import io.treehouses.remote.pojo.ServiceInfo;

public class ServicesDetailsFragment extends BaseServicesFragment {

    View view;
    ImageView logo;
    private ProgressBar progressBar;

    private boolean received = false;
    private boolean infoClicked;
    private int quoteCount;
    private String buildString;
    private int[] versionIntNumber;


    public ServicesDetailsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        mChatService = listener.getChatService();
//        mChatService.updateHandler(mHandler);

//        writeToRPI("treehouses version\n");

        view = inflater.inflate(R.layout.activity_services_details, container, false);
        logo = view.findViewById(R.id.service_logo);
        progressBar = view.findViewById(R.id.progressBar);

        try {
            SVG svg = SVG.getFromString("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<svg\n" +
                    "   xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
                    "   xmlns:cc=\"http://creativecommons.org/ns#\"\n" +
                    "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                    "   xmlns:svg=\"http://www.w3.org/2000/svg\"\n" +
                    "   xmlns=\"http://www.w3.org/2000/svg\"\n" +
                    "   id=\"svg2\"\n" +
                    "   version=\"1.1\"\n" +
                    "   viewBox=\"0 0 400 400\"\n" +
                    "   height=\"250\"\n" +
                    "   width=\"250\">\n" +
                    "  <metadata\n" +
                    "     id=\"metadata28\">\n" +
                    "    <rdf:RDF>\n" +
                    "      <cc:Work\n" +
                    "         rdf:about=\"\">\n" +
                    "        <dc:format>image/svg+xml</dc:format>\n" +
                    "        <dc:type\n" +
                    "           rdf:resource=\"http://purl.org/dc/dcmitype/StillImage\" />\n" +
                    "        <dc:title></dc:title>\n" +
                    "      </cc:Work>\n" +
                    "    </rdf:RDF>\n" +
                    "  </metadata>\n" +
                    "  <defs\n" +
                    "     id=\"defs26\" />\n" +
                    "  <g\n" +
                    "     transform=\"scale(3.8461538,3.8461538)\"\n" +
                    "     id=\"g4171\">\n" +
                    "    <circle\n" +
                    "       style=\"fill:#ffffff;fill-opacity:1;stroke:none;stroke-width:1.5;stroke-linejoin:round;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:0.00392157\"\n" +
                    "       id=\"path4169\"\n" +
                    "       cx=\"52\"\n" +
                    "       cy=\"52\"\n" +
                    "       r=\"52\" />\n" +
                    "    <g\n" +
                    "       id=\"g4158\">\n" +
                    "      <path\n" +
                    "         id=\"path6\"\n" +
                    "         d=\"M 38.22,5.29 C 44.53,3.22 51.78,8.34 51.9,14.98 52.38,20.97 47.04,26.67 40.97,26.3 34.75,26.34 29.42,20.12 30.65,13.96 31.25,9.95 34.29,6.38 38.22,5.29 Z\"\n" +
                    "         style=\"fill:#327bb9\" />\n" +
                    "      <path\n" +
                    "         id=\"path8\"\n" +
                    "         d=\"m 64.4,6.54 c 1,-1.14 3.09,-1 3.5,0.63 1.88,4.61 0.15,9.72 -1.64,14.09 C 61.62,32.22 49.84,39.6 37.94,38.85 29.14,38.44 20.4,33.95 15.43,26.62 c -1.08,-1.65 -2.55,-5.58 0.55,-5.84 4.12,0.47 7.25,3.7 10.98,5.3 9.32,5.08 21.93,2.73 28.82,-5.32 3.86,-4.12 5.18,-9.86 8.62,-14.22 z\"\n" +
                    "         style=\"fill:#327bb9\" />\n" +
                    "      <path\n" +
                    "         id=\"path10\"\n" +
                    "         d=\"m 81.38,15.45 c 1.74,-1.1 4.5,0.14 3.66,2.43 -1.33,4.3 -4.52,7.73 -6.1,11.94 -4.42,9.97 -0.62,22.74 8.48,28.72 3.59,2.62 8.01,3.8 11.54,6.49 1.4,0.86 1.57,3.2 -0.15,3.81 C 95.15,70.44 91,69.46 87.36,68.31 78.01,65.34 70.48,57.4 67.98,47.92 64.66,35.91 69.85,21 81.38,15.45 Z\"\n" +
                    "         style=\"fill:#327bb9\" />\n" +
                    "      <path\n" +
                    "         id=\"path12\"\n" +
                    "         d=\"m 86.26,32.24 c 5.9,-2.6 13.36,1.41 14.52,7.72 1.29,5.72 -2.88,12.02 -8.74,12.8 -5.99,1.18 -12.23,-3.73 -12.44,-9.83 -0.33,-4.5 2.44,-9.06 6.66,-10.69 z\"\n" +
                    "         style=\"fill:#327bb9\" />\n" +
                    "      <path\n" +
                    "         id=\"path14\"\n" +
                    "         d=\"m 7.35,37.53 c 12.52,-1.67 24.85,7.58 28.9,19.22 3.25,9.07 1.66,19.69 -4.15,27.39 -2.42,3.15 -5.28,6.4 -9.26,7.47 -1.16,0.44 -3.04,0 -3.03,-1.52 -0.13,-2.21 1.33,-4.07 2.31,-5.92 2.71,-4.34 5.23,-9.03 5.63,-14.23 C 28.4,61.42 24.34,52.56 17.02,47.99 13.41,45.42 8.88,44.35 5.54,41.41 3.81,39.94 5.49,37.64 7.35,37.53 Z\"\n" +
                    "         style=\"fill:#327bb9\" />\n" +
                    "      <path\n" +
                    "         id=\"path16\"\n" +
                    "         d=\"m 49.68,41.78 c 0.91,-0.07 2.74,-0.19 3.65,-0.26 0.18,2.81 0.38,5.61 0.54,8.41 2.35,-1.53 4.73,-3.02 7.07,-4.56 0.51,0.77 1.51,2.31 2.02,3.07 -2.35,1.51 -4.67,3.05 -7.02,4.56 2.49,1.25 4.96,2.53 7.46,3.78 -0.55,1.09 -1.1,2.18 -1.65,3.27 -2.49,-1.26 -4.99,-2.48 -7.48,-3.75 0.18,2.78 0.34,5.57 0.51,8.35 -1.22,0.08 -2.43,0.16 -3.65,0.24 -0.18,-2.81 -0.38,-5.62 -0.56,-8.42 -2.35,1.55 -4.73,3.05 -7.1,4.57 -0.66,-1.03 -1.31,-2.06 -1.96,-3.09 2.33,-1.47 4.7,-2.91 6.81,-4.69 -2.44,-1.17 -4.85,-2.41 -7.26,-3.63 0.55,-1.09 1.1,-2.18 1.64,-3.27 2.48,1.26 4.98,2.48 7.47,3.74 C 50,47.33 49.85,44.55 49.68,41.78 Z\"\n" +
                    "         style=\"fill:#87b852\" />\n" +
                    "      <path\n" +
                    "         id=\"path18\"\n" +
                    "         d=\"M 11.35,54.43 C 18.81,51.68 27.22,59.29 25.05,67 23.69,74.77 13.09,78.14 7.47,72.63 1.29,67.62 3.71,56.51 11.35,54.43 Z\"\n" +
                    "         style=\"fill:#327bb9\" />\n" +
                    "      <path\n" +
                    "         id=\"path20\"\n" +
                    "         d=\"m 59.43,68.59 c 8.43,-1.8 17.6,0.27 24.36,5.64 3.14,2.47 6.35,5.53 7.04,9.66 0.25,2 -2.1,2.66 -3.58,1.91 C 83.16,84.12 79.69,81.21 75.52,79.68 66.55,76 55.39,78.76 49.1,86.11 c -3.44,3.86 -5.11,8.83 -7.75,13.18 -0.74,1.59 -3.42,2.68 -4.25,0.58 C 35.29,96 36.49,91.54 37.78,87.7 41.07,78.16 49.52,70.61 59.43,68.59 Z\"\n" +
                    "         style=\"fill:#327bb9\" />\n" +
                    "      <path\n" +
                    "         id=\"path22\"\n" +
                    "         d=\"m 59.34,81.43 c 6.25,-3.04 14.46,1.65 14.99,8.57 0.75,5.62 -3.62,11.3 -9.29,11.85 -6.23,1.06 -12.48,-4.57 -12.03,-10.89 0.07,-4.03 2.62,-7.93 6.33,-9.53 z\"\n" +
                    "         style=\"fill:#327bb9\" />\n" +
                    "    </g>\n" +
                    "  </g>\n" +
                    "</svg>");
            PictureDrawable pd = new PictureDrawable(svg.renderToPicture());

            logo.setImageDrawable(pd);
        } catch (SVGParseException e) {
            e.printStackTrace();
        }

        return view;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    String output = (String) msg.obj;
                    moreActions(output);
                    break;
                case Constants.MESSAGE_WRITE:
                    String write_msg = new String((byte[]) msg.obj);
                    Log.d("WRITE", write_msg);
                    break;

            }
        }
    };

    private void onClickInstall(ServiceInfo selected) {
        if (selected.serviceStatus == ServiceInfo.SERVICE_AVAILABLE && checkVersion(versionIntNumber)) {
            performService("Installing", "treehouses services " + selected.name + " install\n", selected.name);
            writeToRPI("treehouses remote services available\n");
        }
        else if (selected.serviceStatus == ServiceInfo.SERVICE_AVAILABLE && !checkVersion(versionIntNumber)) {
            performService("Installing", "treehouses services " + selected.name + " up\n", selected.name);
            writeToRPI("treehouses remote services available\n");
        }
        else if (installedOrRunning(selected)) {
            showDeleteDialog(selected);
        }
    }

    private void onClickStart(ServiceInfo selected) {
        if (selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED && checkVersion(versionIntNumber)) {
            performService("Starting", "treehouses services " + selected.name + " up\n", selected.name);
        }
        else if (selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED && !checkVersion(versionIntNumber)) {
            performService("Starting", "treehouses services " + selected.name + " start\n", selected.name);
        }
        else if (selected.serviceStatus == ServiceInfo.SERVICE_RUNNING) {
            performService("Stopping", "treehouses services " + selected.name + " stop\n", selected.name);
        }
    }

    private void onClickRestart(ServiceInfo selected) {
        if (selected.serviceStatus != ServiceInfo.SERVICE_AVAILABLE) performService("Restarting", "treehouses services " + selected.name + " restart\n", selected.name);

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

    private void onClickInfo(ServiceInfo selected) {
        progressBar.setVisibility(View.VISIBLE);
        writeToRPI("treehouses services " + selected.name + " info");
        infoClicked = true;
        quoteCount = 0;
        buildString = "";
    }

    private void increaseQuoteCount(String output) {
        quoteCount += getQuoteCount(output);
        buildString += output;
        if (output.startsWith("https://")) {
            buildString += "\n\n";
        }
        if (quoteCount >= 2) {
            showInfoDialog(buildString);
            progressBar.setVisibility(View.GONE);
        }
    }
    private boolean isLocalUrl(String output) {
        return output.contains(".") && output.contains(":") && output.length() < 20 && !received;
    }

    private void moreActions(String output) {
        if (isLocalUrl(output)) {
            received = true;
            openLocalURL(output);
            progressBar.setVisibility(View.GONE);
        }
        else if (output.contains(".onion") && ! received) {
            received = true;
            openTorURL(output);
            progressBar.setVisibility(View.GONE);
        }
        else if (infoClicked) {
            increaseQuoteCount(output);
        }
        else if (isVersionNumber(output)) {

            writeToRPI("treehouses remote services available\n");
        }
    }

    private void setOnClick(View v, int id, String command, AlertDialog alertDialog) {
        v.findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToRPI(command);
                alertDialog.dismiss();
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean isVersionNumber(String s) {
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
        versionIntNumber = intParts;
        return true;
    }
}












