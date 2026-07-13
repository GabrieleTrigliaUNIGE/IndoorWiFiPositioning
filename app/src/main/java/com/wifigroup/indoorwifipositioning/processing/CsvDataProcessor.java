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

public class CsvDataProcessor extends Thread {
    private static final String TAG = "CsvDataProcessor";
    private final List<String> csvLines;
    private final IOnProcessingCompleted listener;

    public CsvDataProcessor(List<String> csvLines, IOnProcessingCompleted listener) {
        this.csvLines = csvLines;
        this.listener = listener;
    }

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

                    Map<Integer, Double> dataForAp = meanData.get(ap);

                    // Se fosse nulla, saltiamo questo giro
                    if (dataForAp == null) continue;

                    SimpleRegression logRegression = new SimpleRegression();
                    for (Map.Entry<Integer, Double> entry : dataForAp.entrySet()) {
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

                    Map<Integer, Double> dataForAp = meanData.get(ap);

                    // Se fosse nulla, saltiamo questo giro
                    if (dataForAp == null) continue;

                    WeightedObservedPoints points = new WeightedObservedPoints();
                    for (Map.Entry<Integer, Double> entry : dataForAp.entrySet()) {
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

            Map<Integer, List<Integer>> dataForAp = grouped.get(ap);

            if (dataForAp == null) continue;

            Map<Integer, Double> distMeanMap = new HashMap<>();

            for (Map.Entry<Integer, List<Integer>> entry : dataForAp.entrySet()) {
                double sum = 0;
                for (int val : entry.getValue()) sum += val;
                distMeanMap.put(entry.getKey(), sum / entry.getValue().size());
            }
            means.put(ap, distMeanMap);
        }
        return means;
    }
}
