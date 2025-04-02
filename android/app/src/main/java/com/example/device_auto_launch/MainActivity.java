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
                        String time = call.argument("time");
                        rebootDevice(time);
                        result.success(null);
                    } else if (call.method.equals("lockTheDevice")) {
                        lockTheDevice();
                        result.success(null);
                    }
                    else {
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
    static void rebootDevice(String time) {
        Log.d("MainActivity","scheduleReboot");

        //Script for rebooting the device for every 2 minutes.
        /*String script = "#!/system/bin/sh\n" +
                "while true; do\n" +
                "sleep 120\n"+
                "    reboot\n" +
                "done";*/

        //Script for waking the device for every 2 minutes of interval.
        /*String script = "#!/system/bin/sh\n" +
                "while true; do\n" +
                "    sleep 120\n" +
                "    input keyevent 26\n" +
                "    sleep 1\n" +
                "    input touchscreen swipe 930 880 930 380\n" +
                "    sleep 1\n" +
                "    input text 1234\n" +
                "    sleep 1\n" +
                "    input keyevent 66 \n" +
                "    am force-stop com.example.device_auto_launch \n"+
                "    sleep 2\n"+
                "    am start -n com.example.device_auto_launch/.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER\n" +
                "done";

        try {
            // Write the script to a file
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("echo '" + script + "' > /data/local/tmp/reboot_scheduler.sh\n");
            os.writeBytes("chmod +x /data/local/tmp/reboot_scheduler.sh\n");
            os.writeBytes("nohup sh /data/local/tmp/reboot_scheduler.sh &\n");
            os.writeBytes("exit\n");
            os.flush();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Script executed successfully.");
            } else {
                System.err.println("Script execution failed. Exit code: " + exitCode);
            }        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }*/

        //Script for waking the device at speific time.
        Log.d("MainActivity",time);
        String script =
                "#!/system/bin/sh\n" +
                        "TARGET_TIME=\"" +time+ "\"  # Set the desired time (24-hour format)\n" +
                        "while true; do\n" +
                        "    CURRENT_TIME=$(date +%H:%M)\n" +
                        "    if [ \"$CURRENT_TIME\" = \"$TARGET_TIME\" ]; then\n" +
                        "        # Perform the desired task\n" +
                        "        input keyevent 26\n" +
                        "        sleep 1\n" +
                        "        input touchscreen swipe 930 880 930 380\n" +
                        "        sleep 1\n" +
                        "        input text 1234\n" +
                        "        sleep 1\n" +
                        "        input keyevent 66\n" +
                        "        am force-stop com.example.device_auto_launch\n" +
                        "        sleep 5\n" +
                        "        am start -n com.example.device_auto_launch/.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER\n" +
                        "        sleep 60  # Wait for 1 minute to prevent multiple executions\n" +
                        "    fi\n" +
                        "    sleep 10  # Check the time every 10 seconds\n" +
                        "done";

        try {
            // Open a root shell
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            // Write the script to a local file
            os.writeBytes("echo '" + script.replace("'", "'\\''") + "' > /data/local/tmp/scheduled_task.sh\n");
            os.writeBytes("chmod +x /data/local/tmp/scheduled_task.sh\n");
            os.writeBytes("nohup sh /data/local/tmp/scheduled_task.sh &\n");
            os.writeBytes("exit\n");
            os.flush();

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Script executed successfully.");
            } else {
                System.err.println("Script execution failed. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
