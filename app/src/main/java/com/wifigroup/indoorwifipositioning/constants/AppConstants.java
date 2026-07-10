package com.wifigroup.indoorwifipositioning.constants;

import java.util.LinkedHashMap;

public class AppConstants {
    public static final String[] ACCESS_POINTS = {
            "AP1", "AP2", "AP3", "AP4"
    };

    public static final LinkedHashMap<Integer, Integer> REQUIRED_MEASUREMENTS = new LinkedHashMap<>();

    static {
        REQUIRED_MEASUREMENTS.put(1, 3);
        REQUIRED_MEASUREMENTS.put(2, 4);
        REQUIRED_MEASUREMENTS.put(3, 6);
        REQUIRED_MEASUREMENTS.put(4, 8);
        REQUIRED_MEASUREMENTS.put(5, 10);
        REQUIRED_MEASUREMENTS.put(6, 12);
        REQUIRED_MEASUREMENTS.put(7, 15);
    }

    public static final String CSV_MEASUREMENTS_FILE = "MISURE_AP_TOT.csv";
    public static final String CSV_COORDINATES_FILE = "AP_COORDINATES.csv";
}
