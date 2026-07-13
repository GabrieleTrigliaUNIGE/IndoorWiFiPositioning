package com.wifigroup.indoorwifipositioning.constants;

import java.util.LinkedHashMap;

public class AppConstants {
    public static final String[] ACCESS_POINTS = {
            "AP1", "AP2", "AP3", "AP4"
    };

    public static final LinkedHashMap<Double, Integer> REQUIRED_MEASUREMENTS = new LinkedHashMap<>();

    static {
        REQUIRED_MEASUREMENTS.put(0.7, 3);
        REQUIRED_MEASUREMENTS.put(1.4, 4);
        REQUIRED_MEASUREMENTS.put(2.1, 6);
        REQUIRED_MEASUREMENTS.put(2.8, 8);
        REQUIRED_MEASUREMENTS.put(3.5, 10);
        REQUIRED_MEASUREMENTS.put(4.2, 12);
        REQUIRED_MEASUREMENTS.put(4.9, 15);
    }

    public static final String CSV_MEASUREMENTS_FILE = "MISURE_AP_TOT.csv";
    public static final String CSV_COORDINATES_FILE = "AP_COORDINATES.csv";
}
