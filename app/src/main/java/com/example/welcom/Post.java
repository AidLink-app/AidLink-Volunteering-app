package com.example.welcom;
import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Post implements Serializable {
    private String postId;
    private String title;
    private String description;
    private Timestamp date;
    private String location;
    private String organizationId;
    private String category;
    private String imageUrl;
    private String whatsapp_link;

    private boolean activeStatus = true;

    private String creatorEmail;

    private List<String> registeredUsers = new ArrayList<>();


    // Default Constructor (Required for Firestore)
    public Post() {
    }

    // Constructor with all fields
    public Post(String title, String description, Timestamp date, String location,
                String organization, String category, String imageUrl, String whatsapp_link) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.organizationId = organization;
        this.category = category;
        this.imageUrl = imageUrl;
        this.whatsapp_link = whatsapp_link;
        this.activeStatus = true;
    }

    // Getters
    public String getPostId() {
        return postId;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getOrganization() {
        return organizationId;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public List<String> getRegisteredUsers() {
        return registeredUsers;
    }


    public void setApprovedUsers(List<String> approvedUsers) {
        this.registeredUsers = approvedUsers;
    }

    public boolean isActive(){
        return activeStatus;
    }

    public String getWhatsapp_link() { return whatsapp_link;}

    // Setters
    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public void setActiveStatus(boolean status){
        this.activeStatus = status;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setOrganization(String organization) {
        this.organizationId = organization;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setWhatsapp_link(String whatsapp_link) { this.whatsapp_link = whatsapp_link;}
    public void setRegisteredUsers(List<String> registeredUsers) {
        this.registeredUsers = registeredUsers;
    }

    // Helper method to convert Post object to Map
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("activeStatus", this.activeStatus);
        result.put("title", this.title);
        result.put("description", this.description);
        result.put("date", this.date);
        result.put("location", this.location);
        result.put("whatsappLink", this.whatsapp_link);
        result.put("category", this.category);
        result.put("imageUrl", this.imageUrl);
        return result;
    }
}