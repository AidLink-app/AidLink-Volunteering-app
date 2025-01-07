package com.example.welcom;

public class Post {
    private String title;
    private String description;
    private String date;
    private String location;
    private String organization;
    private String category;
    private String imageUrl;

    // Default Constructor (Required for Firestore)
    public Post() {
    }

    // Constructor with all fields
    public Post(String title, String description, String date, String location,
                String organization, String category, String imageUrl) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.organization = organization;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getOrganization() {
        return organization;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}