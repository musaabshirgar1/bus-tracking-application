package com.example.bustrackingsystem;

public class CentroidLocation {
    private String driverId;
    private boolean isCentroidFormed;
    private double centroidLat;
    private double centroidLon;

    CentroidLocation(){

    }
    public CentroidLocation(String driverId, boolean isCentroidFormed, double centroidLat, double centroidLon) {
        this.driverId = driverId;
        this.isCentroidFormed = isCentroidFormed;
        this.centroidLat = centroidLat;
        this.centroidLon = centroidLon;
    }

    public String getDriverId() {
        return driverId;
    }

    public String setDriverId(String driverId) {
        this.driverId = driverId;
        return driverId;
    }

    public boolean isCentroidFormed() {
        return isCentroidFormed;
    }

    public boolean setCentroidFormed(boolean centroidFormed) {
        isCentroidFormed = centroidFormed;
        return centroidFormed;
    }

    public double getCentroidLat() {
        return centroidLat;
    }

    public double setCentroidLat(double centroidLat) {
        this.centroidLat = centroidLat;
        return centroidLat;
    }

    public double getCentroidLon() {
        return centroidLon;
    }

    public double setCentroidLon(double centroidLon) {
        this.centroidLon = centroidLon;
        return centroidLon;
    }
}
