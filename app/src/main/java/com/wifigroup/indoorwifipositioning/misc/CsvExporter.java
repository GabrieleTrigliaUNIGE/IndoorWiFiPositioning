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
 * A background thread designed to export collected Wi-Fi measurements to a CSV file.
 * <p>
 * This class extends {@link Thread} to perform file I/O operations asynchronously,
 * preventing UI freezes. It handles the formatting and saving of the training phase
 * data into the device's public Downloads directory. The result of the operation
 * is communicated back to the caller via a callback interface.
 * </p>
 *
 * @author WiFiGroup
 * @version 1.1.0
 */
public class CsvExporter extends Thread {

    private static final String TAG = "CsvExporter";

    private final String[] accessPoints;
    private final LinkedHashMap<Integer, Integer> requiredMap;
    private final Map<String, Map<Integer, List<Integer>>> measureData;
    private final ICsvExportCompleted listener;

    /**
     * Initializes a new background thread to export the recorded RSSI measurements.
     *
     * @param accessPoints an array containing the SSIDs of the target Access Points
     * @param requiredMap  a map defining the required distances for calibration
     * @param measureData  the structured data collection containing the recorded RSSI values,
     *                     mapped by AP SSID and then by distance
     * @param listener     the callback interface to be notified upon export completion
     */
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

    /**
     * Executes the file writing operation in a background thread.
     * <p>
     * The generated file is named using a timestamp format (e.g., "misure_wifi_YYYYMMDD_HHMMSS.csv")
     * and contains a standard header followed by the data rows formatted as:
     * {@code AP, Distanza_m, Misura_n, dBm}. Upon completion or failure,
     * it triggers the appropriate {@link ICsvExportCompleted#onExportDone(boolean, String)} callback.
     * </p>
     */
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