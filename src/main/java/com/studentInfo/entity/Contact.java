package com.studentInfo.entity;


import java.sql.Timestamp;

public class Contact {
    private int id;
    private String mobNo;
    private String city;
    private String address;
    private int studentId;
    private Timestamp createdAt;

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // Constructors
    public Contact() {}

    public Contact(int id, String mobNo, String city, String address, int studentId) {
        this.id = id;
        this.mobNo = mobNo;
        this.city = city;
        this.address = address;
        this.studentId = studentId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMobNo() { return mobNo; }
    public void setMobNo(String mobNo) { this.mobNo = mobNo; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
}
