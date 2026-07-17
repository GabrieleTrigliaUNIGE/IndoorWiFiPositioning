package com.wifigroup.indoorwifipositioning.interfaces;

public interface ICsvExportCompleted {
    void onExportDone(boolean isSuccess, String filename);
}
