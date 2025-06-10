package com.example.tfgdanielmario;

import com.google.firebase.Timestamp;

public class Message {
    private String user;
    private String text;
    private String userId;
    private Timestamp timestamp;

    public Message() {} // Necesario para Firestore

    public Message(String user, String text, String userId, Timestamp timestamp) {
        this.user = user;
        this.text = text;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getUser() { return user; }
    public String getText() { return text; }
    public String getUserId() { return userId; }
    public Timestamp getTimestamp() { return timestamp; }

    public void setUser(String user) { this.user = user; }
    public void setText(String text) { this.text = text; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
