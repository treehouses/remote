package io.treehouses.remote.bases;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public abstract class PermissionActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_WIFI = 111;
    private static final int PERMISSION_REQUEST_CODE_CAMERA = 112;

    public boolean checkPermission(String strPermission) {
        int result = ContextCompat.checkSelfPermission(this, strPermission);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    public void requestPermission() {
        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) || !checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION) || !checkPermission(Manifest.permission.CHANGE_WIFI_STATE)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, PERMISSION_REQUEST_WIFI);
        }
    }

}
