package com.wifigroup.indoorwifipositioning.interfaces;

/**
 * Callback interface used to deliver the results of a Wi-Fi scan operation.
 *
 * @author WiFiGroup
 * @version 1.0.0
 */
public interface IWiFiScanCompleted {

    /**
     * Called when a new RSSI measurement is available for a specific Access Point.
     *
     * @param ssid the Service Set Identifier (name) of the detected Access Point
     * @param dBm the Received Signal Strength Indicator (RSSI) value in decibel-milliwatts
     */
    void onWifiScanCompleted(String ssid, int dBm);
}