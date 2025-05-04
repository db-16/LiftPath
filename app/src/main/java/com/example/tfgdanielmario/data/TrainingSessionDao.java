package com.example.tfgdanielmario.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TrainingSessionDao {
    @Insert
    void insertSession(TrainingSession session);

    @Query("SELECT * FROM training_sessions WHERE user_id = :userId ORDER BY date DESC")
    List<TrainingSession> getSessionsForUser(int userId);
}
