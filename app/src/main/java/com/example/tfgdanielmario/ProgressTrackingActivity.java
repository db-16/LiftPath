package com.example.tfgdanielmario;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tfgdanielmario.data.AppDatabase;
import com.example.tfgdanielmario.data.ExerciseRecord;
import com.example.tfgdanielmario.data.TrainingSession;

import java.util.ArrayList;
import java.util.List;

public class ProgressTrackingActivity extends AppCompatActivity {

    private ListView lvProgress;
    private int currentUserId = 1; // For demo, static user id. In real app, get logged in user id.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_tracking);

        lvProgress = findViewById(R.id.lvProgress);

        loadProgressData();
    }

    private void loadProgressData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<TrainingSession> sessions = db.trainingSessionDao().getSessionsForUser(currentUserId);

            List<String> progressList = new ArrayList<>();

            for (TrainingSession session : sessions) {
                StringBuilder sessionSummary = new StringBuilder();
                sessionSummary.append("Date: ").append(session.date).append("\n");

                List<ExerciseRecord> records = db.exerciseRecordDao().getRecordsForSession(session.id);
                for (ExerciseRecord record : records) {
                    sessionSummary.append(record.exerciseName)
                            .append(": ")
                            .append(record.repetitions)
                            .append(" reps, Load: ")
                            .append(record.load)
                            .append("\n");
                }
                progressList.add(sessionSummary.toString());
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, progressList);
                lvProgress.setAdapter(adapter);
            });
        }).start();
    }
}
