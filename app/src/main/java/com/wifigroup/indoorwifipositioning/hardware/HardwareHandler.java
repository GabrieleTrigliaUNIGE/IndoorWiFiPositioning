package com.wifigroup.indoorwifipositioning.hardware;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Utility class for verifying and managing the state of required hardware sensors.
 * <p>
 * This class ensures that both the Wi-Fi adapter and the device's Location (GPS)
 * services are turned on, which are strict requirements for scanning Access Points
 * in modern Android versions.
 * </p>
 *
 * @author WiFiGroup
 * @version 1.1.0-alpha
 */
public class HardwareHandler {

    /**
     * Checks if both Wi-Fi and Location services are currently enabled on the device.
     * <p>
     * If either service is disabled, this method notifies the user via a {@link Toast}
     * and automatically launches the corresponding Android system settings panel
     * so the user can enable the required hardware.
     * </p>
     *
     * @param context the application or activity context used to display toasts and launch intents
     * @param wifiManager the {@link WifiManager} system service to check the Wi-Fi state
     * @return {@code true} if both Wi-Fi and Location services are enabled; {@code false} otherwise
     */
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