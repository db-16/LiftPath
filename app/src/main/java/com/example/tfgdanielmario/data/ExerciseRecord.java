package com.example.tfgdanielmario.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "exercise_records")
public class ExerciseRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "session_id")
    public int sessionId;

    public String exerciseName;
    public int repetitions;
    public float load; // weight or load used

    public ExerciseRecord(int sessionId, String exerciseName, int repetitions, float load) {
        this.sessionId = sessionId;
        this.exerciseName = exerciseName;
        this.repetitions = repetitions;
        this.load = load;
    }
}
