package com.studentInfo.entity;


import org.springframework.web.multipart.MultipartFile;

public class Student {
    private int id;
    private String name;
    private String fatherName;
    private String profilePhoto;
    private String aadharPath;
    private String aadharNumber;
    private MultipartFile profilePhotoFile;
    private MultipartFile aadharFile;

    // Constructors
    public Student() {}

    public Student(int id, String name, String fatherName, String aadharNumber) {
        this.id = id;
        this.name = name;
        this.fatherName = fatherName;
        this.aadharNumber = aadharNumber;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public String getAadharPath() { return aadharPath; }
    public void setAadharPath(String aadharPath) { this.aadharPath = aadharPath; }

    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String aadharNumber) { this.aadharNumber = aadharNumber; }

    public MultipartFile getProfilePhotoFile() { return profilePhotoFile; }
    public void setProfilePhotoFile(MultipartFile profilePhotoFile) {
        this.profilePhotoFile = profilePhotoFile;
    }

    public MultipartFile getAadharFile() { return aadharFile; }
    public void setAadharFile(MultipartFile aadharFile) {
        this.aadharFile = aadharFile;
    }
}