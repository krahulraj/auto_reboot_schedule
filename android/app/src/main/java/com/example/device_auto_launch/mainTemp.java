/*
package com.example.device_auto_launch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.example.kiosk_mode/native";
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize DevicePolicyManager and Admin Component
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);

        // Check if the app is the device owner
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (devicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                Log.d("MainActivity", "App is the device owner");
                // Start Lock Task Mode to bypass screen lock
                startLockTask();
                Log.d("MainActivity", "Lock Task Mode started");
            } else {
                Log.d("MainActivity", "App is not the device owner");
                // Handle the case where the app is not the device owner
                requestDeviceAdminPermission();
            }
        }

        checkAndRequestOverlayPermission();

        setAlarm();

        // Set MethodChannel to listen for Flutter calls
        new MethodChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler((call, result) -> {
                    if (call.method.equals("rebootDevice")) {
                        rebootDevice();
                        result.success(null);
                    } else if (call.method.equals("setAlarm")) {
                        setAlarm();
                        result.success(null);
                    } else {
                        result.notImplemented();
                    }
                });
    }

    private void requestDeviceAdminPermission() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Please activate device admin to allow your app to manage the device.");
        startActivityForResult(intent, 1);

    }
    private void checkAndRequestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission is required for some features to work. Please grant it.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 2);
            } else {
                Log.d("MainActivity", "Overlay permission already granted");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.d("MainActivity", "Device Admin enabled successfully");
            } else {
                Log.d("MainActivity", "Device Admin enabling failed");
            }
        }
    }

    // Reboot the device (only if app is device owner)
    private void rebootDevice() {
        try {
            Log.d("MainActivity", "Rebooted successfully");
            Process process = Runtime.getRuntime().exec("su");
            OutputStream outputStream = process.getOutputStream();
            outputStream.write("reboot\n".getBytes());
            outputStream.flush();
            outputStream.close();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.d("MainActivity", "Error while rebooting: " + e.toString());
        }
    }

    // Set a repeating alarm
    private void setAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long intervalMillis = 2 * 60 * 1000; // 5 minutes
        long triggerAtMillis = System.currentTimeMillis() + intervalMillis;

        // Use setExactAndAllowWhileIdle for API >= 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }

        Log.d("MainActivity", "Alarm scheduled successfully for 5 minutes.");
    }

}
*/
