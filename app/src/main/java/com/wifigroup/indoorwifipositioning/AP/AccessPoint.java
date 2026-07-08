package com.wifigroup.indoorwifipositioning.AP;

public class AccessPoint {

    public String ssid;

    public double x = 0.0;
    public double y = 0.0;

    public double p_0 = 0.0;
    public double n = 0.0;

    public double coeffA = 0.0;
    public double coeffB = 0.0;
    public double coeffC = 0.0;

    public AccessPoint(String ssid) {
        this.ssid = ssid;
    }
}