package com.example.tfgdanielmario;

public class User {

    private String id;          // UID de Firebase Authentication
    private String name;
    private double weight;
    private double goalWeight;
    private String idRoutine;
    private String mail;
    private String goal; // objetivo como texto

    public User() {}

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setGoalWeight(double goalWeight) {
        this.goalWeight = goalWeight;
    }

    public void setIdRoutine(String idRoutine) {
        this.idRoutine = idRoutine;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public double getGoalWeight() {
        return goalWeight;
    }

    public String getIdRoutine() {
        return idRoutine;
    }

    public String getMail() {
        return mail;
    }

    public String getGoal() {
        return goal;
    }
}
