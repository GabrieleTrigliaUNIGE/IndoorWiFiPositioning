package com.wifigroup.indoorwifipositioning.processing;

import android.util.Log;

import com.wifigroup.indoorwifipositioning.AP.AccessPoint;
import com.wifigroup.indoorwifipositioning.interfaces.IOnProcessingCompleted;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes CSV data containing RSSI measurements to calibrate Access Points.
 * <p>
 * This thread computes the mean RSSI values for each distance per Access Point.
 * It then performs concurrent calculations using two different mathematical approaches:
 * a logarithmic path loss model and a second-degree polynomial regression.
 * The computed coefficients are used to configure the {@link AccessPoint} objects,
 * which are subsequently passed back to the UI via the {@link IOnProcessingCompleted} callback.
 * </p>
 *
 * @author WiFiGroup
 * @version 1.0.0
 */
public class CsvDataProcessor extends Thread {

    private static final String TAG = "CsvDataProcessor";

    private final List<String> csvLines;

    private final IOnProcessingCompleted listener;

    /**
     * Constructs a new CsvDataProcessor.
     *
     * @param csvLines a list of raw data strings read from the CSV file
     * @param listener the callback interface to notify upon completion
     */
    public CsvDataProcessor(List<String> csvLines, IOnProcessingCompleted listener) {
        this.csvLines = csvLines;
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Executes the data processing pipeline in a background thread. It calculates
     * the mean RSSI values, spawns two parallel threads to compute the logarithmic
     * and polynomial models, and finally notifies the listener with the calibrated data.
     * </p>
     */
    @Override
    public void run() {
        Log.i(TAG, "Avvio calcolo Medie dal CSV...");

        try {
            Map<String, Map<Integer, Double>> meanData = calculateMeans(csvLines);

            Map<String, double[]> LogMap = new HashMap<>();
            Map<String, double[]> PolyMap = new HashMap<>();

            // THREAD 1: LOGARITMICO
            Thread logThread = new Thread(() -> {
                for (String ap : meanData.keySet()) {
                    SimpleRegression logRegression = new SimpleRegression();
                    for (Map.Entry<Integer, Double> entry : meanData.get(ap).entrySet()) {
                        logRegression.addData(10.0 * Math.log10(entry.getKey()), entry.getValue());
                    }
                    double rssi0 = logRegression.getIntercept();
                    double n = -logRegression.getSlope();

                    LogMap.put(ap, new double[]{rssi0, n});
                }
            });

            // THREAD 2: POLINOMIALE
            Thread polyThread = new Thread(() -> {
                for (String ap : meanData.keySet()) {
                    WeightedObservedPoints points = new WeightedObservedPoints();
                    for (Map.Entry<Integer, Double> entry : meanData.get(ap).entrySet()) {
                        points.add(entry.getValue(), entry.getKey());
                    }
                    PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
                    double[] coeff = fitter.fit(points.toList());

                    PolyMap.put(ap, new double[]{coeff[2], coeff[1], coeff[0]});
                }
            });

            logThread.start();
            polyThread.start();

            logThread.join();
            polyThread.join();

            Map<String, AccessPoint> finalModels = new HashMap<>();

            for (String apName : meanData.keySet()) {
                AccessPoint ap = new AccessPoint(apName);

                double[] logData = LogMap.get(apName);

                if (logData != null) {
                    ap.p_0 = logData[0];
                    ap.n = logData[1];
                    Log.i(TAG, String.format("AP: %s | p_0: %.2f | n: %.3f", apName, ap.p_0, ap.n));
                }

                double[] polyData = PolyMap.get(apName);

                if (polyData != null) {
                    ap.coeffA = polyData[0];
                    ap.coeffB = polyData[1];
                    ap.coeffC = polyData[2];
                    Log.i(TAG, String.format("AP: %s | Poly: (%.4f)*R^2 + (%.4f)*R + (%.2f)", apName, ap.coeffA, ap.coeffB, ap.coeffC));
                }

                finalModels.put(apName, ap);
            }

            if(listener != null){
                listener.onProcessingDone(finalModels);
            }

            Log.i(TAG, "Calcolo parallelo terminato");

        } catch (InterruptedException e) {
            Log.i(TAG, "Sincronizzazione thread interrotta: " + e.getMessage());
        } catch (Exception e) {
            Log.i(TAG, "Errore generale: " + e.getMessage());
        }
    }

    /**
     * Parses the raw CSV lines and calculates the average RSSI for each distance per Access Point.
     *
     * @param lines the list of raw comma-separated strings to be processed
     * @return a map associating each Access Point SSID with a map of distances and their average RSSI
     */
    private Map<String, Map<Integer, Double>> calculateMeans(List<String> lines) {
        Map<String, Map<Integer, List<Integer>>> grouped = new HashMap<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length < 4) continue;
            grouped.computeIfAbsent(parts[0], k -> new HashMap<>())
                    .computeIfAbsent(Integer.parseInt(parts[1]), k -> new ArrayList<>())
                    .add(Integer.parseInt(parts[3]));
        }

        Map<String, Map<Integer, Double>> means = new HashMap<>();
        for (String ap : grouped.keySet()) {
            Map<Integer, Double> distMeanMap = new HashMap<>();
            for (Map.Entry<Integer, List<Integer>> entry : grouped.get(ap).entrySet()) {
                double sum = 0;
                for (int val : entry.getValue()) sum += val;
                distMeanMap.put(entry.getKey(), sum / entry.getValue().size());
            }
            means.put(ap, distMeanMap);
        }
        return means;
    }
}