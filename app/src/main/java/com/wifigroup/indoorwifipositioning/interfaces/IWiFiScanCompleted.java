package com.wifigroup.indoorwifipositioning.interfaces;

public interface IWiFiScanCompleted {
    void onWifiScanCompleted(String ssid, int dBm);   // dBm = -999 se AP non trovato
}
