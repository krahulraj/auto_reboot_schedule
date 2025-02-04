package com.example.device_auto_launch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.example.device_auto_launch/native";
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

        // Set MethodChannel to listen for Flutter calls
        new MethodChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler((call, result) -> {
                    if (call.method.equals("rebootDevice")) {
                        rebootDevice();
                        result.success(null);
                    } else if (call.method.equals("lockTheDevice")) {
                        lockTheDevice();
                        result.success(null);
                    } else {
                        result.notImplemented();
                    }
                });
    }

    private void lockTheDevice(){
        Log.d("MainActivity","Device is locked");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {

            new Handler().postDelayed(()->{
                        devicePolicyManager.lockNow();
            }
            ,5000);
        }
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
    static void rebootDevice() {
        Log.d("MainActivity","scheduleReboot");

        String script = "#!/system/bin/sh\n" +
                "while true; do\n" +
                "sleep 120\n"+
                "    reboot\n" +
                "done";

        /*String script = "#!/system/bin/sh\n" +
                "LAUNCH_TIME=\"18:39\"\n" +
                "while true; do\n" +
                "    CURRENT_TIME=$(date +%H:%M)\n" +
                "    if [ \"$CURRENT_TIME\" = \"$LAUNCH_TIME\" ]; then\n" +
                "        input keyevent 26\n" +
                "        input touchscreen swipe 930 880 930 380\n"+
                "        input text  1234 \n"+
                "        sleep 3\n" +
                "        am start -n com.example.device_auto_launch/.MainActivity\n" +
                "        sleep 60\n" +
                "    fi\n" +
                "    sleep 5\n" +
                "done";*/


        try {
            // Write the script to a file
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("echo '" + script + "' > /data/local/tmp/reboot_scheduler.sh\n");
            os.writeBytes("chmod +x /data/local/tmp/reboot_scheduler.sh\n");
            os.writeBytes("nohup sh /data/local/tmp/reboot_scheduler.sh &\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
