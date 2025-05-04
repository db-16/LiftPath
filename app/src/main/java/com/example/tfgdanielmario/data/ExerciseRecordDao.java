package com.example.tfgdanielmario.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExerciseRecordDao {
    @Insert
    void insertExerciseRecord(ExerciseRecord record);

    @Query("SELECT * FROM exercise_records WHERE session_id = :sessionId")
    List<ExerciseRecord> getRecordsForSession(int sessionId);
}
