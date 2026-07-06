package com.wifigroup.indoorwifipositioning.processing;

import android.util.Log;

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
            // CALCOLO DELLE MEDIE
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

                    // Inserisce array di 3 valori riordinati: [A, B, C]
                    PolyMap.put(ap, new double[]{coeff[2], coeff[1], coeff[0]});
                }
            });

            logThread.start();
            polyThread.start();

            logThread.join();
            polyThread.join();

            // VERIFICA DELLE TABELLE FINALI ───
            Log.i(TAG, "TABELLA LOG-DISTANZA");
            for (Map.Entry<String, double[]> entry : LogMap.entrySet()) {
                double[] parametri = entry.getValue();
                // parametri[0] = RSSI0, parametri[1] = n
                Log.i(TAG, String.format("AP: %s | RSSI_0: %.2f | n: %.3f",
                        entry.getKey(), parametri[0], parametri[1]));
            }

            Log.i(TAG, "-----------------------------------------------");
            Log.i(TAG, "TABELLA POLINOMIALE");
            for (Map.Entry<String, double[]> entry : PolyMap.entrySet()) {
                double[] parametri = entry.getValue();
                // parametri[0] = A, parametri[1] = B, parametri[2] = C
                Log.i(TAG, String.format("AP: %s | d = (%.4f)*R^2 + (%.4f)*R + (%.2f)",
                        entry.getKey(), parametri[0], parametri[1], parametri[2]));
            }

            if(listener != null){
                listener.onProcessingDone(LogMap, PolyMap);
            }

            Log.i(TAG, "Calcolo parallelo terminato! Passo le due tabelle al Fragment.");

            // RESTITUISCO LE TABELLE
            if (listener != null) {
                listener.onProcessingDone(LogMap, PolyMap);
            }

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
