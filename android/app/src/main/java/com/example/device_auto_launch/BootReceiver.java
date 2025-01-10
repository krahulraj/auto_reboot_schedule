package com.example.device_auto_launch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())) {
            MainActivity.rebootDevice();
            // Device has booted, attempt to unlock the device and launch the app

            // Acquire WakeLock to keep the device awake while performing the tasks
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK,
                    "DeviceAutoLaunch::WakeLock");
//            wakeLock.acquire(10*60*1000L ); // Keep device awake for 10 minutes

            try {
                // Unlock the device with the PIN
                unlockDeviceWithPin("1234");

                if(!wakeLock.isHeld()){
                    wakeLock.acquire(10*60*1000l);
                    Log.d("BootReceiver","Wake lock acquired");
                }
               new Handler().postDelayed(()->{
                   launchApp(context);
               },5000);

            }
            catch (Exception e){
                Log.d("BootReceiver","Error occured while launching");
            }
                // Release the WakeLock after the tasks are done
                new Handler().postDelayed(() -> {
                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                        Log.d("BootReceiver", "Wake lock released");
                    }
                }, 5*60*1000l);
        }
    }

    private void unlockDeviceWithPin(String pin) {
        try {
            // Get root access (make sure device is rooted)
            Process process = Runtime.getRuntime().exec("su");
            OutputStream outputStream = process.getOutputStream();

            // Press the power button to wake up the screen
            outputStream.write("input keyevent 26\n".getBytes());

            // Swipe up to show the PIN entry screen
            outputStream.write("input touchscreen swipe 930 880 930 380\n".getBytes());

            // Enter the PIN
            outputStream.write(("input text " + pin + "\n").getBytes());

            outputStream.flush();
            outputStream.close();

            // Wait for the commands to execute
            process.waitFor();
            Log.d("BootReceiver", "Device unlocked successfully");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.d("BootReceiver", "Error unlocking device: " + e.getMessage());
        }
    }

    private void launchApp(Context context) {
        // Launch your app after the device is unlocked
        Intent launchIntent = null; // Replace with your app's package name
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.CUPCAKE) {
            launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.example.device_auto_launch");
        }
        if (launchIntent != null) {
            // App exists, launch it
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
            Log.d("BootReceiver", "App launched successfully");
        } else {
            Log.d("BootReceiver", "App not found");
        }
    }
}
