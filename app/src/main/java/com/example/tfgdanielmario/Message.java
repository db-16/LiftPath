package com.example.tfgdanielmario;

public class Message {
    private String user;
    private String text;

    public Message() {} // Necesario para Firestore

    public Message(String user, String text) {
        this.user = user;
        this.text = text;
    }

    public String getUser() { return user; }
    public String getText() { return text; }
}
