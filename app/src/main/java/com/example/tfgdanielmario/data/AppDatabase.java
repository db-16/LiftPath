package com.example.tfgdanielmario.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {User.class, TrainingSession.class, ExerciseRecord.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract TrainingSessionDao trainingSessionDao();
    public abstract ExerciseRecordDao exerciseRecordDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "tfg_danielmario")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}
