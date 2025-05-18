package com.example.tfgdanielmario;


public class ProgressEntry {

    private String exerciseId;   // Id o nombre del ejercicio
    private double weight;       // Peso usado
    private int repetitions;     // Repeticiones realizadas

    // Constructor vacío necesario para Firestore
    public ProgressEntry() {}

    public ProgressEntry(String exerciseId, double weight, int repetitions) {
        this.exerciseId = exerciseId;
        this.weight = weight;
        this.repetitions = repetitions;
    }

    // Getters y setters
    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }
}
