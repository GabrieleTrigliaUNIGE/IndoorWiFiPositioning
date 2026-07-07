package com.wifigroup.indoorwifipositioning.AP;

public class AccessPoint {

    public String ssid;

    // Coordinate fisiche nella stanza (le imposteremo nel DemoFragment)
    public double x = 0.0;
    public double y = 0.0;

    // Parametri Log-Distanza
    public double p_0 = 0.0;
    public double n = 0.0;

    // Parametri Polinomiali (A*x^2 + B*x + C)
    public double coeffA = 0.0;
    public double coeffB = 0.0;
    public double coeffC = 0.0;

    public AccessPoint(String ssid) {
        this.ssid = ssid;
    }
}