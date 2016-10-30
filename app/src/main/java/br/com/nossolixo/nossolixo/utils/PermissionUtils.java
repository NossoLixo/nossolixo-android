package br.com.nossolixo.nossolixo.utils;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

public abstract class PermissionUtils {
    public static void requestPermission(AppCompatActivity activity, int requestId, String permission) {
        ActivityCompat.requestPermissions(activity, new String[] { permission }, requestId);
    }

    public static boolean isPermissionGranted(String[] grantPermissions,
                                              int[] grantResults,
                                              String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }
}