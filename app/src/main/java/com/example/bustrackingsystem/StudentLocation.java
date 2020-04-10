package com.example.bustrackingsystem;

public class StudentLocation {
    private String studentBusRoute;
    private  String studentId;
    private double studentLat;
    private double studentLon;
    private String driverId;

    StudentLocation(){
    }

    StudentLocation(String studentBusRoute, String studentId ,double studentLat, double studentLon, String driverId) {
        this.studentBusRoute = studentBusRoute;
        this.studentLat = studentLat;
        this.studentLon = studentLon;
        this.studentId = studentId;
        this.driverId = driverId;
    }

    public String getStudentBusRoute() {
        return studentBusRoute;
    }

    String setStudentBusRoute(String studentBusRoute) {
        this.studentBusRoute = studentBusRoute;
        return studentBusRoute;
    }

    public double getStudentLat() {
        return studentLat;
    }

    double setStudentLat(double studentLat) {
        this.studentLat = studentLat;
        return studentLat;
    }

    public double getStudentLon() {
        return studentLon;
    }

     double setStudentLon(double studentLon) {
        this.studentLon = studentLon;
         return studentLon;
     }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDriverId() {
        return driverId;
    }

    String setDriverId(String driverId) {
        this.driverId = driverId;
        return driverId;
    }
}
