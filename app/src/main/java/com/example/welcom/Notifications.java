package com.example.welcom;

import java.util.Date;

public class Notifications {
    private String id;  // Firestore ID (optional)
    private String userID;
    private String title;
    private String message;
    private Date timestamp;

    // Empty constructor (required for Firestore)
    public Notifications() {}

    public Notifications(String id, String userID, String title, String message, Date timestamp) {
        this.id = id;
        this.userID = userID;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getUserID() { return userID; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public Date getTimestamp() { return timestamp; }

    public void setId(String id) { this.id = id; }
    public void setUserID(String userID) { this.userID = userID; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
