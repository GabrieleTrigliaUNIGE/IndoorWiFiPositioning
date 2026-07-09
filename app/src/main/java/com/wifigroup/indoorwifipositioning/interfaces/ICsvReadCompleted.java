package com.wifigroup.indoorwifipositioning.interfaces;

import java.util.List;

public interface ICsvReadCompleted {
    void onCsvReadDone(List<String> dataRaw, String fileName);
}