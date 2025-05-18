package com.example.tfgdanielmario;

import java.util.Date;

public class WeightRecord {
    private double weight;
    private Date date;

    public WeightRecord() {
        // Constructor vacío requerido para Firestore
    }

    public WeightRecord(double weight, Date date) {
        this.weight = weight;
        this.date = date;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
} 