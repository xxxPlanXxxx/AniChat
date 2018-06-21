package com.planx.anichat.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class MyUtils {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public static final int  CALL_IN = 0x01;
    public static final int  CALL_OUT = 0x02;
    public static final int  SCAN_OPEN_PHONE=0x03;
    public static final int  PHONE_CAMERA= 0x04;
    public static final int  PHONE_CROP=0x05;

    private static long timeLast;

    public static boolean isFastlyClick(){
        if (System.currentTimeMillis() - timeLast < 1500){
            timeLast = System.currentTimeMillis();
            return true;
        }else {
            timeLast = System.currentTimeMillis();
            return false;
        }

    }

}
