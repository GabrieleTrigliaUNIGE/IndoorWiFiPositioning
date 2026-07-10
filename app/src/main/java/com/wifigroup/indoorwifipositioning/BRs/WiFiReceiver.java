package com.wifigroup.indoorwifipositioning.BRs;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.wifigroup.indoorwifipositioning.interfaces.IWiFiScanCompleted;

import java.util.List;

/**
 * Handles the reception of Wi-Fi scan results from the Android system.
 * <p>
 * This BroadcastReceiver intercepts the scan completion intents. It implements
 * logic to discard stale or cached results and can operate in two modes:
 * filtering the results for a specific target SSID (used during the training phase)
 * or broadcasting all detected Access Points (used during the real-time positioning phase).
 * </p>
 *
 * @author WiFiGroup
 * @version 1.1.0
 */
public class WiFiReceiver extends BroadcastReceiver {

    private final String TAG = "WiFiReceiver";

    private WifiManager wifiManager = null;
    private IWiFiScanCompleted wiFiScanCompleted = null;
    private String targetSSID = null;

    /**
     * Constructs a new WiFiReceiver.
     *
     * @param wifiManager the system's Wi-Fi manager used to retrieve scan results
     * @param wiFiScanCompleted the callback interface to notify when a valid scan is processed
     */
    public WiFiReceiver(WifiManager wifiManager, IWiFiScanCompleted wiFiScanCompleted) {
        this.wifiManager = wifiManager;
        this.wiFiScanCompleted = wiFiScanCompleted;
    }

    /**
     * Sets a specific target SSID to filter the incoming scan results.
     * <p>
     * When a target is set, the receiver will only trigger the callback for this
     * specific Access Point. If set to null, the receiver will process all detected networks.
     * </p>
     *
     * @param ssid the Service Set Identifier (network name) to look for, or null to clear the filter
     */
    public void setTargetSSID(String ssid) {
        this.targetSSID = ssid;
    }

    /**
     * Called when the BroadcastReceiver receives an Intent broadcast.
     * <p>
     * This method verifies the freshness of the Wi-Fi scan results. If the results are
     * from the system cache, they are discarded and an error code (-998) is sent.
     * If fresh, it searches for the target SSID (if configured) or iterates through all
     * available networks, passing the extracted RSSI levels (in dBm) to the callback.
     * </p>
     *
     * @param context the Context in which the receiver is running
     * @param intent the Intent being received, containing the scan status
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean scanFresh = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

        Log.i(TAG, "Scansione fresca: " + scanFresh);

        if (!scanFresh) {
            Log.i(TAG, "Risultati dalla cache — misura scartata");
            wiFiScanCompleted.onWifiScanCompleted(targetSSID, -998);
            return;
        }

        List<ScanResult> wifiScan = wifiManager.getScanResults();

        if (targetSSID != null) {

            int dBm = -999;
            for (ScanResult result : wifiScan) {
                if (targetSSID != null && targetSSID.equals(result.SSID)) {
                    dBm = result.level;
                    Log.i(TAG, "Trovato " + targetSSID + " → " + dBm + " dBm");
                    break;
                }
            }

            if (dBm == -999) {
                Log.i(TAG, "SSID \"" + targetSSID + "\" non trovato nella scansione");
            }

            wiFiScanCompleted.onWifiScanCompleted(targetSSID, dBm);
        } else {
            for (ScanResult result : wifiScan) {
                wiFiScanCompleted.onWifiScanCompleted(result.SSID, result.level);
            }
            Log.i(TAG, "Inviati " + wifiScan.size() + " risultati grezzi al DemoFragment.");
        }
    }
}