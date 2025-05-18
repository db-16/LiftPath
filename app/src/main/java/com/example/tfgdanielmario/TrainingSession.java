package com.example.tfgdanielmario;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class TrainingSession {

    private String id;
    private String userId;
    private String name;

    // Constructor necesario para Firestore
    public TrainingSession() {}

    public TrainingSession(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
