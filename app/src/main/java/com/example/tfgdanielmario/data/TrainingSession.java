package com.example.tfgdanielmario.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "training_sessions")
public class TrainingSession {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "date")
    public String date; // ISO format date string

    public TrainingSession(int userId, String date) {
        this.userId = userId;
        this.date = date;
    }
}
