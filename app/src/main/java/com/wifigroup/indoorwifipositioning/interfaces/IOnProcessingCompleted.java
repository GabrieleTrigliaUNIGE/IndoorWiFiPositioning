package com.wifigroup.indoorwifipositioning.interfaces;

import java.util.Map;

public interface IOnProcessingCompleted {
    void onProcessingDone(Map<String, double[]> logModels, Map<String, double[]> polyModels);
    void onError(String errorMessage);
}