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
    private String targetSSID = null;           // SSID da cercare nella scansione

    public WiFiReceiver(WifiManager wifiManager, IWiFiScanCompleted wiFiScanCompleted) {
        this.wifiManager = wifiManager;
        this.wiFiScanCompleted = wiFiScanCompleted;
    }

    /** Imposta quale SSID cercare prima di chiamare wifiManager.startScan() */
    public void setTargetSSID(String ssid) {
        this.targetSSID = ssid;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive — cerco SSID: " + targetSSID);

        List<ScanResult> wifiScan = wifiManager.getScanResults();

        int dBm = -999;     // valore sentinella: AP non trovato

        for (ScanResult result : wifiScan) {
            if (targetSSID != null && targetSSID.equals(result.SSID)) {
                dBm = result.level;
                Log.i(TAG, "Trovato " + targetSSID + " → " + dBm + " dBm");
                break;
            }
        }

        if (dBm == -999) {
            Log.w(TAG, "SSID \"" + targetSSID + "\" non trovato nella scansione");
        }

        wiFiScanCompleted.onWifiScanCompleted(targetSSID, dBm);
    }
}