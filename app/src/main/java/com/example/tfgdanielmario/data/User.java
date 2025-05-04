package com.example.tfgdanielmario.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;
    public String password;
    public float weight;
    public String goal; // "lose", "gain", "maintain"

    public User(String username, String password, float weight, String goal) {
        this.username = username;
        this.password = password;
        this.weight = weight;
        this.goal = goal;
    }
}
