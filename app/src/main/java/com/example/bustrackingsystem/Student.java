package com.example.bustrackingsystem;

public class Student {
    private String studentId;
    private String studentName;
    private String studentRollNumber;
    private String studentEmail;
    private String studentBranch;
    private String studentBusRoute;


    Student(){
    }

    public Student(String studentId, String studentName, String studentRollNumber, String studentEmail, String studentBranch, String studentBusRoute) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentRollNumber = studentRollNumber;
        this.studentEmail = studentEmail;
        this.studentBranch = studentBranch;
        this.studentBusRoute = studentBusRoute;

    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getStudentBranch() {
        return studentBranch;
    }

    public String getStudentBusRoute() {
        return studentBusRoute;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public void setStudentBranch(String studentBranch) {
        this.studentBranch = studentBranch;
    }

    public String setStudentBusRoute(String studentBusRoute) {
        this.studentBusRoute = studentBusRoute;
        return studentBusRoute;
    }
}
