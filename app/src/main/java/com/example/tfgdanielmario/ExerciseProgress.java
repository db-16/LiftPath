package com.example.tfgdanielmario;

import java.io.Serializable;
import java.util.Date;

public class ExerciseProgress implements Serializable {
    private Date timestamp;
    private int reps;
    private double weight;

    // Constructor vacío necesario para Firebase
    public ExerciseProgress() {}

    public ExerciseProgress(Date timestamp, int reps, double weight) {
        this.timestamp = timestamp;
        this.reps = reps;
        this.weight = weight;
    }

    // Getters y setters
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
} 