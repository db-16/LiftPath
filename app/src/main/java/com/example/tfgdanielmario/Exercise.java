package com.example.tfgdanielmario;

public class Exercise {
    private String name;
    private int reps;

    public int getInitialWeight() {
        return initialWeight;
    }

    public void setInitialWeight(int initialWeight) {
        this.initialWeight = initialWeight;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    private int initialWeight;

    public Exercise(String name, int reps, int initialWeight) {
        this.name = name;
        this.reps = reps;
        this.initialWeight = initialWeight;
    }

    public String getName() {
        return name;
    }

}
