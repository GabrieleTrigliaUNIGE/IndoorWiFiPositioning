package com.wifigroup.indoorwifipositioning.AP;

/**
 * Represents a physical Wi-Fi Access Point used for indoor positioning.
 * <p>
 * This data class holds the necessary configuration for an Access Point,
 * including its physical 2D coordinates and the mathematical coefficients
 * required to estimate distances from RSSI values using either a logarithmic
 * or a polynomial model.
 * </p>
 *
 * @author WiFiGroup
 * @version 1.1.0
 */
public class AccessPoint {

    /** The Service Set Identifier (network name) of the Access Point. */
    public String ssid;

    /** The physical X coordinate of the Access Point in the reference map. */
    public double x = 0.0;

    /** The physical Y coordinate of the Access Point in the reference map. */
    public double y = 0.0;

    /** The reference RSSI value at 1 meter distance (used in the logarithmic model). */
    public double p_0 = 0.0;

    /** The path loss exponent (used in the logarithmic model). */
    public double n = 0.0;

    /** The quadratic coefficient (A) for the second-degree polynomial model. */
    public double coeffA = 0.0;

    /** The linear coefficient (B) for the second-degree polynomial model. */
    public double coeffB = 0.0;

    /** The constant term (C) for the second-degree polynomial model. */
    public double coeffC = 0.0;

    /**
     * Constructs a new AccessPoint with the specified SSID.
     *
     * @param ssid the network name of the Access Point
     */
    public AccessPoint(String ssid) {
        this.ssid = ssid;
    }
}