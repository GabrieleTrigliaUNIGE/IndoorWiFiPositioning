package com.wifigroup.indoorwifipositioning.misc;

import android.os.Environment;
import android.util.Log;

import com.wifigroup.indoorwifipositioning.interfaces.ICsvExportCompleted;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provides utility methods to export collected Wi-Fi measurements to a CSV file.
 * <p>
 * This class handles the formatting and file I/O operations required to save
 * the training phase data into the device's public Downloads directory. The exported
 * CSV file can then be used for external analysis or calibration of the positioning models.
 * </p>
 *
 * @author WiFiGroup
 * @version 1.0.0
 */
public class CsvExporter extends Thread {

    private static final String TAG = "CsvExporter";

    /**
     * Exports the recorded RSSI measurements to a CSV file in the device's Downloads folder.
     * <p>
     * The generated file is named using a timestamp format (e.g., "misure_wifi_YYYYMMDD_HHMMSS.csv")
     * and contains a standard header followed by the data rows formatted as:
     * {@code AP, Distanza_m, Misura_n, dBm}. A toast notification is displayed to the user
     * upon success or failure.
     * </p>
     *
     * @param context the application context used to display UI toast notifications
     * @param accessPoints an array containing the SSIDs of the target Access Points
     * @param requiredMap a map defining the required distances for calibration
     * @param measureData the structured data collection containing the recorded RSSI values,
     * mapped by AP SSID and then by distance
     */
    private final String[] accessPoints;
    private final LinkedHashMap<Integer, Integer> requiredMap;
    private final Map<String, Map<Integer, List<Integer>>> measureData;
    private final ICsvExportCompleted listener;

    public CsvExporter(
            String[] accessPoints,
            LinkedHashMap<Integer, Integer> requiredMap,
            Map<String, Map<Integer, List<Integer>>> measureData,
            ICsvExportCompleted listener) {

        this.accessPoints = accessPoints;
        this.requiredMap = requiredMap;
        this.measureData = measureData;
        this.listener = listener;
    }

    @Override
    public void run() {

        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File file = new File(dir, "misure_wifi_" + ts + ".csv");

            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {

                pw.println("AP,Distanza_m,Misura_n,dBm");

                for (String ap : accessPoints) {
                    for (int dist : requiredMap.keySet()) {

                        Map<Integer, List<Integer>> byDist = measureData.get(ap);
                        if (byDist == null) continue;

                        List<Integer> values = byDist.get(dist);
                        if (values == null) continue;

                        for (int i = 0; i < values.size(); i++) {
                            pw.println(ap + "," + dist + "," + (i + 1) + "," + values.get(i));
                        }
                    }
                }
            }

            Log.i(TAG, "CSV saved: " + file.getAbsolutePath());
            if (listener != null) {
                listener.onExportDone(true, file.getName());
            }

        } catch (IOException e) {
            Log.i(TAG, "Error writing CSV: " + e.getMessage());
            if (listener != null) {
                listener.onExportDone(false, e.getMessage());
            }
        }
    }
}