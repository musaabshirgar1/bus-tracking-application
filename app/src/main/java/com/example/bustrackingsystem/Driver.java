package com.example.bustrackingsystem;

public class Driver {

    private String driverId;
    private String driverName;
    private String driverPhoneNumber;
    private String driverEmail;
    private String driverBusName;

    Driver(){
    }

    public Driver(String driverId, String driverName, String driverPhoneNumber, String driverEmail, String driverBusName) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.driverPhoneNumber = driverPhoneNumber;
        this.driverEmail = driverEmail;
        this.driverBusName = driverBusName;

    }

    public String getDriverId() {
        return driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverPhoneNumber() {
        return driverPhoneNumber;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public String getDriverBusName() {
        return driverBusName;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setDriverPhoneNumber(String driverPhoneNumber) {
        this.driverPhoneNumber = driverPhoneNumber;
    }

    public void setDriverEmail(String driverEmail) {
        this.driverEmail = driverEmail;
    }

    String setDriverBusName(String driverBusName) {
        this.driverBusName = driverBusName;
        return driverBusName;
    }

}
