package com.wifigroup.indoorwifipositioning.misc;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

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

public class CsvExporter {

    private static final String TAG = "CsvExporter";

    public static void exportToDownloads(
            Context context,
            String[] accessPoints,
            LinkedHashMap<Integer, Integer> requiredMap,
            Map<String, Map<Integer, List<Integer>>> measureData) {

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
            Toast.makeText(context, "CSV saved in Download:\n" + file.getName(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Log.i(TAG, "Error writing CSV: " + e.getMessage());
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}