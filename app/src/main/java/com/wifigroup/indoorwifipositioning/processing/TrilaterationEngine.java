package com.wifigroup.indoorwifipositioning.processing;

import android.util.Log;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import com.wifigroup.indoorwifipositioning.AP.AccessPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrilaterationEngine {

    private static final String TAG = "TrilaterationEngine";

    public static double[] calculatePosition(
            Map<String, Integer> liveRssi,
            Map<String, AccessPoint> roomMap,
            boolean isLogModel) {

        List<double[]> positionsList = new ArrayList<>();
        List<Double> distancesList = new ArrayList<>();

        for (String ssid : liveRssi.keySet()) {
            if (roomMap.containsKey(ssid)) {

                AccessPoint ap = roomMap.get(ssid);
                int currentRssi = liveRssi.get(ssid);
                double distance = 0;

                if (isLogModel) {
                    // Aggiornato con ap.p_0
                    distance = Math.pow(10.0, (ap.p_0 - currentRssi) / (10.0 * ap.n));
                } else {
                    distance = (ap.coeffA * Math.pow(currentRssi, 2)) + (ap.coeffB * currentRssi) + ap.coeffC;
                }

                if (distance > 0) {
                    positionsList.add(new double[]{ap.x, ap.y});
                    distancesList.add(distance);
                }
            }
        }

        if (positionsList.size() < 3) return null;

        double[][] positions = positionsList.toArray(new double[0][0]);
        double[] distances = new double[distancesList.size()];
        for (int i = 0; i < distancesList.size(); i++) distances[i] = distancesList.get(i);

        try {
            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(
                    new TrilaterationFunction(positions, distances),
                    new LevenbergMarquardtOptimizer()
            );
            LeastSquaresOptimizer.Optimum optimum = solver.solve();
            return optimum.getPoint().toArray();
        } catch (Exception e) {
            Log.i(TAG, "Errore trilaterazione: " + e.getMessage());
            return null;
        }
    }
}
