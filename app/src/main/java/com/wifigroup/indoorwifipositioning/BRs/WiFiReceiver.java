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

public class WiFiReceiver extends BroadcastReceiver {

    private final String TAG = "WiFiReceiver";

    private WifiManager wifiManager = null;
    private IWiFiScanCompleted wiFiScanCompleted = null;
    private String targetSSID = null;

    public WiFiReceiver(WifiManager wifiManager, IWiFiScanCompleted wiFiScanCompleted) {
        this.wifiManager = wifiManager;
        this.wiFiScanCompleted = wiFiScanCompleted;
    }

    public void setTargetSSID(String ssid) {
        this.targetSSID = ssid;
    }

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