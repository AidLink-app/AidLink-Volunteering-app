package com.example.welcom;
import java.io.Serializable;
public class User implements Serializable {

    private String userId;
    private String email;
    private String role; // "organization" or "volunteer"
    private String imageUrl;
    private String pdfUrl;
    private String description;
    private String name;
    private String phone;

    private String fcmToken;

    public User() {}
    public User(String email, String role, String imageUrl, String pdfUrl, String description, String name, String phone) {
        this.email = email;
        this.role = role;
        this.imageUrl = imageUrl;
        this.pdfUrl = pdfUrl;
        this.description = description;
        this.name = name;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
