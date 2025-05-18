package com.example.tfgdanielmario;

import java.util.Date;
import java.util.List;

public class TrainingHistory {
    private String id;
    private String userId;
    private String sessionId;
    private Date timestamp;
    private List<ExerciseRecord> exercises;

    public TrainingHistory() {} // Constructor vacío para Firestore

    public TrainingHistory(String id, String userId, String sessionId, Date timestamp, List<ExerciseRecord> exercises) {
        this.id = id;
        this.userId = userId;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.exercises = exercises;
    }

    // Getters y Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public List<ExerciseRecord> getExercises() { return exercises; }
    public void setExercises(List<ExerciseRecord> exercises) { this.exercises = exercises; }
}
