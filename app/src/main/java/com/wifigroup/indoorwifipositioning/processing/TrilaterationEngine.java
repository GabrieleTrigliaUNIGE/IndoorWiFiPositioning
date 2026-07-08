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

/**
 * Provides utility methods to calculate the device's physical position using trilateration.
 * <p>
 * This engine utilizes the Levenberg-Marquardt algorithm to solve the non-linear
 * least squares optimization problem. It converts real-time RSSI readings into physical
 * distances based on either a logarithmic path loss model or a polynomial regression model,
 * and calculates the intersection of the resulting circles to estimate the user's 2D location.
 * </p>
 *
 * @author WiFiGroup
 * @version 1.0.0
 */
public class TrilaterationEngine {

    private static final String TAG = "TrilaterationEngine";

    /**
     * Calculates the 2D coordinates of the device based on real-time Wi-Fi measurements.
     * <p>
     * The method requires at least 3 valid Access Points to perform the trilateration.
     * Distances are estimated dynamically using the calibration coefficients stored in
     * the provided {@link AccessPoint} objects.
     * </p>
     *
     * @param liveRssi a map containing the current RSSI readings (in dBm) for each detected AP's SSID
     * @param roomMap a map of known Access Points, containing their physical coordinates and calibration models
     * @param isLogModel {@code true} to estimate distance using the logarithmic path loss model,
     * {@code false} to use the second-degree polynomial regression model
     * @return a {@code double} array containing the estimated [X, Y] coordinates,
     * or {@code null} if the calculation fails or there are not enough valid APs
     */
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