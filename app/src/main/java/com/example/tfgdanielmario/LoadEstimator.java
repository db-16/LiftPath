package com.example.tfgdanielmario;

import com.example.tfgdanielmario.data.ExerciseRecord;
import com.example.tfgdanielmario.data.TrainingSession;
import com.example.tfgdanielmario.data.AppDatabase;

import android.content.Context;

import java.util.List;

public class LoadEstimator {

    private AppDatabase db;
    private int userId;

    public LoadEstimator(Context context, int userId) {
        this.db = AppDatabase.getInstance(context);
        this.userId = userId;
    }

    /**
     * Estimate the next session's load and repetitions for each exercise based on previous sessions and user goal.
     * This is a simple example that increases load by 5% if user is gaining or maintaining weight,
     * decreases by 5% if losing weight, and keeps repetitions constant.
     */
    public void estimateNextSession() {
        new Thread(() -> {
            List<TrainingSession> sessions = db.trainingSessionDao().getSessionsForUser(userId);
            if (sessions.isEmpty()) {
                // No previous sessions, no estimation
                return;
            }

            TrainingSession lastSession = sessions.get(0);
            List<ExerciseRecord> lastRecords = db.exerciseRecordDao().getRecordsForSession(lastSession.id);

            // Get user goal
            String goal = db.userDao().getUserById(userId).goal;

            for (ExerciseRecord record : lastRecords) {
                float newLoad = record.load;
                int newReps = record.repetitions;

                if ("gain".equalsIgnoreCase(goal) || "maintain".equalsIgnoreCase(goal)) {
                    newLoad = record.load * 1.05f; // increase load by 5%
                } else if ("lose".equalsIgnoreCase(goal)) {
                    newLoad = record.load * 0.95f; // decrease load by 5%
                }

                // Here you could save or return these estimations for UI or reports
                // For now, just print to log (in real app, use proper logging)
                System.out.println("Exercise: " + record.exerciseName + ", Next Load: " + newLoad + ", Reps: " + newReps);
            }
        }).start();
    }
}
