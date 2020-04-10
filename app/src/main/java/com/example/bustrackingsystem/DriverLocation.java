package com.example.bustrackingsystem;

public class DriverLocation {
    private String driverBusName;
    private String driverId;
    private double driverLat;
    private double driverLon;

    DriverLocation(){

    }

    DriverLocation(String driverBusName,String driverId, double driverLat, double driverLon) {
        this.driverBusName = driverBusName;
        this.driverId = driverId;
        this.driverLat = driverLat;
        this.driverLon = driverLon;
    }

    public String getDriverBusName() {
        return driverBusName;
    }

    String setDriverBusName(String driverBusName) {
        this.driverBusName = driverBusName;
        return driverBusName;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public double getDriverLat() {
        return driverLat;
    }

    double setDriverLat(double driverLat) {
        this.driverLat = driverLat;
        return driverLat;
    }

    public double getDriverLon() {
        return driverLon;
    }

    double setDriverLon(double driverLon) {
        this.driverLon = driverLon;
        return driverLon;
    }
}
