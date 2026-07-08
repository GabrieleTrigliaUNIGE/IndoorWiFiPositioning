package com.wifigroup.indoorwifipositioning.interfaces;

import com.wifigroup.indoorwifipositioning.AP.AccessPoint;
import java.util.Map;

/**
 * Callback interface used to notify when the RSSI data processing and
 * Access Point calibration are completed.
 *
 * @author WiFiGroup
 * @version 1.0.0
 */
public interface IOnProcessingCompleted {

    /**
     * Called when the CSV data processing finishes successfully.
     *
     * @param calibratedAps a map containing the calibrated {@link AccessPoint} objects,
     * mapped by their respective SSIDs
     */
    void onProcessingDone(Map<String, AccessPoint> calibratedAps);
}