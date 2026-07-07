package com.wifigroup.indoorwifipositioning.interfaces;

import com.wifigroup.indoorwifipositioning.AP.AccessPoint;
import java.util.Map;

public interface IOnProcessingCompleted {
    void onProcessingDone(Map<String, AccessPoint> calibratedAps);
}