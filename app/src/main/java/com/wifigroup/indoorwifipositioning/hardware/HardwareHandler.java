package com.wifigroup.indoorwifipositioning.hardware;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.Toast;

public class HardwareHandler {
    public static boolean isHardwareReady(Context context, WifiManager wifiManager) {

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(context, "WiFi OFF", Toast.LENGTH_LONG).show();

                Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                context.startActivity(panelIntent);

            return false;
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isLocationEnabled()) {
            Toast.makeText(context, "GPS OFF", Toast.LENGTH_LONG).show();

            Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(locationIntent);

            return false;
        }

        return true;
    }
}
