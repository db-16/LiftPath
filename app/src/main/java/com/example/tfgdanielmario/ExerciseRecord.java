package com.example.tfgdanielmario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExerciseRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String sessionId;
    private String userId;
    private String exerciseName;
    private int reps;
    private int sets;
    private float initialWeight;
    private float estimatedWeight;
    private List<ExerciseProgress> progress;

    // Constructor vacío necesario para Firebase
    public ExerciseRecord() {
        progress = new ArrayList<>();
    }

    public ExerciseRecord(String sessionId, String exerciseName, int reps, int sets, float initialWeight) {
        this.sessionId = sessionId;
        this.exerciseName = exerciseName;
        this.reps = reps;
        this.sets = sets;
        this.initialWeight = initialWeight;
        this.estimatedWeight = initialWeight;
        this.progress = new ArrayList<>();
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public float getInitialWeight() {
        return initialWeight;
    }

    public void setInitialWeight(float initialWeight) {
        this.initialWeight = initialWeight;
    }

    public List<ExerciseProgress> getProgress() {
        return progress;
    }

    public void setProgress(List<ExerciseProgress> progress) {
        this.progress = progress;
    }

    public float getEstimatedWeight() {
        return estimatedWeight;
    }

    public void setEstimatedWeight(float estimatedWeight) {
        this.estimatedWeight = estimatedWeight;
    }

    public void addProgress(ExerciseProgress progress) {
        if (this.progress == null) {
            this.progress = new ArrayList<>();
        }
        this.progress.add(progress);
    }

    @Override
    public String toString() {
        return exerciseName + " - " + sets + " series (" + initialWeight + " kg)";
    }
}
