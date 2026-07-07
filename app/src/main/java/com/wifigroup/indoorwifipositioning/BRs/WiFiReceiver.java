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
        boolean scanFresh = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

        Log.i(TAG, "Scansione fresca: " + scanFresh);

        // Se Android ha restituito dati dalla cache, rifiutiamo la misura
        if (!scanFresh) {
            Log.i(TAG, "Risultati dalla cache — misura scartata");
            wiFiScanCompleted.onWifiScanCompleted(targetSSID, -998);  // -998 = codice "cache"
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
            // MODO DEMO (Rete da pesca): Nessun target impostato, passiamo tutto!
            for (ScanResult result : wifiScan) {
                // Inviamo ogni singolo Access Point trovato al DemoFragment
                wiFiScanCompleted.onWifiScanCompleted(result.SSID, result.level);
            }
            Log.i(TAG, "Inviati " + wifiScan.size() + " risultati grezzi al DemoFragment.");

        }
    }
}