package com.example.device_auto_launch;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

public class AlarmReceiver extends BroadcastReceiver {
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "A larm is received");

        // Wake the device up if it is asleep
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp::WakeLockTag");

        wakeLock.acquire(10 * 60 * 1000L); // Acquire for 10 minutes

        Log.d("AlarmReceiver", "Device is waked");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }
        adminComponent = new ComponentName(context, MyDeviceAdminReceiver.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && devicePolicyManager.isDeviceOwnerApp(context.getPackageName())) {
            // Reboot device if the app is device owner
            devicePolicyManager.reboot(adminComponent);
        }
        try {
            Log.d("AlarmReceiver","Rebooted sucessfully");

            Process process = Runtime.getRuntime().exec("su");
            OutputStream outputStream = process.getOutputStream();
            outputStream.write("reboot\n".getBytes());
            outputStream.flush();
            outputStream.close();
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.d("AlarmReceiver","Error while reboting"+e.toString());
        }
        finally {
            // Release the wake lock after some time
            new Handler().postDelayed(() -> {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                    Log.d("AlarmReceiver", "Wake lock released");
                }
            }, 10000); // Adjust as needed
        }
    }
}
