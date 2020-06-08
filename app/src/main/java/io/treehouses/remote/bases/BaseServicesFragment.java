package io.treehouses.remote.bases;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import io.treehouses.remote.pojo.ServiceInfo;
import io.treehouses.remote.pojo.ServicesData;

public class BaseServicesFragment extends BaseFragment {
    private static final int[] MINIMUM_VERSION = {1, 14, 1};
    private String startJson = "";
    private boolean gettingJSON = false;
    protected ServicesData servicesData;

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
            intent.setAction(Intent.ACTION_VIEW);
            startActivity(intent);
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

    protected boolean installedOrRunning(ServiceInfo selected) {
        return selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED || selected.serviceStatus == ServiceInfo.SERVICE_RUNNING;
    }

    private void constructServiceList(ServicesData servicesData, ArrayList<ServiceInfo> services) {
        if (servicesData == null || servicesData.getAvailable() == null) {
            Toast.makeText(getContext(), "Error Occurred. Please Refresh", Toast.LENGTH_SHORT).show();
            return;
        }
        services.clear();

        addServicesToList(services);
        getServices(services);

        formatList(services);
    }

    private void addServicesToList( ArrayList<ServiceInfo> services){
        for (String service : servicesData.getAvailable()) {
            if (inServiceList(service, services) == -1) {
                services.add(new ServiceInfo(service, ServiceInfo.SERVICE_AVAILABLE, servicesData.getIcon().get(service),
                        servicesData.getInfo().get(service), servicesData.getAutorun().get(service)));
            }
        }
    }

    private void getServices(ArrayList<ServiceInfo> services){
        for (String service : servicesData.getRunning()) {
            if (inServiceList(service, services) == -1) continue;
                runningOrInstalled(service, services, true);
        }
        for (String service : servicesData.getRunning()) {
            if (inServiceList(service, services) == -1) continue;
                runningOrInstalled(service, services, false);
        }
    }

    private void runningOrInstalled(String service, ArrayList<ServiceInfo> services, boolean installedOrRunning){
        if(installedOrRunning)
            services.get(inServiceList(service, services)).serviceStatus = ServiceInfo.SERVICE_INSTALLED;
        else
            services.get(inServiceList(service, services)).serviceStatus = ServiceInfo.SERVICE_RUNNING;
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

    private int isError(String output) {
        if(output.toLowerCase().startsWith("usage:") ||
                output.toLowerCase().contains("error") ||
                output.toLowerCase().contains("unknown"))
            return 0;
        else
            return -1;
    }

    protected int performAction(String output, ArrayList<ServiceInfo> services) {
        int i = isError(output);
        startJson += output.trim();

        if (gettingJSON && startJson.endsWith("}}") ) {
            startJson += output.trim();
            try {
                JSONObject jsonObject = new JSONObject(startJson);
                servicesData = new Gson().fromJson(jsonObject.toString(), ServicesData.class);
                constructServiceList(servicesData, services);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            gettingJSON = false;
            i = 1;
        }
        else if (output.trim().startsWith("{")) {
            Log.d("STARTED", "performAction: ");
            startJson = output.trim();
            gettingJSON = true;
        }
        return i;
    }

    protected boolean isTorURL(String output, boolean received) {
        return output.contains(".onion") && !received;
    }

    protected boolean isLocalUrl(String output, boolean received) {
        return output.contains(".") && output.contains(":") && output.length() < 25 && !received;
    }
}
