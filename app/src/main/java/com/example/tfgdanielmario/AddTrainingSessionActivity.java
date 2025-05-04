package com.example.tfgdanielmario;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tfgdanielmario.data.AppDatabase;
import com.example.tfgdanielmario.data.ExerciseRecord;
import com.example.tfgdanielmario.data.TrainingSession;

import java.util.ArrayList;
import java.util.List;

public class AddTrainingSessionActivity extends AppCompatActivity {

    private EditText etDate, etExerciseName, etRepetitions, etLoad;
    private Button btnAddExercise, btnSaveSession;

    private List<ExerciseRecord> exerciseRecords = new ArrayList<>();
    private int currentUserId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_progress_exercise);

        etExerciseName = findViewById(R.id.etExerciseName);
        etRepetitions = findViewById(R.id.etRepetitions);
        etLoad = findViewById(R.id.etLoad);

        btnAddExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addExerciseRecord();
            }
        });

        btnSaveSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTrainingSession();
            }
        });
    }

    private void addExerciseRecord() {
        String exerciseName = etExerciseName.getText().toString().trim();
        String repetitionsStr = etRepetitions.getText().toString().trim();
        String loadStr = etLoad.getText().toString().trim();

        if (exerciseName.isEmpty() || repetitionsStr.isEmpty() || loadStr.isEmpty()) {
            Toast.makeText(this, "Please fill all exercise fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int repetitions;
        float load;
        try {
            repetitions = Integer.parseInt(repetitionsStr);
            load = Float.parseFloat(loadStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid repetitions or load", Toast.LENGTH_SHORT).show();
            return;
        }

        ExerciseRecord record = new ExerciseRecord(0, exerciseName, repetitions, load);
        exerciseRecords.add(record);

        etExerciseName.setText("");
        etRepetitions.setText("");
        etLoad.setText("");

        Toast.makeText(this, "Exercise added", Toast.LENGTH_SHORT).show();
    }

    private void saveTrainingSession() {
        String date = etDate.getText().toString().trim();
        if (date.isEmpty()) {
            Toast.makeText(this, "Please enter the date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (exerciseRecords.isEmpty()) {
            Toast.makeText(this, "Add at least one exercise", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            TrainingSession session = new TrainingSession(currentUserId, date);
            db.trainingSessionDao().insertSession(session);

            // Get the inserted session id (Room does not return id on insert, so we fetch latest)
            List<TrainingSession> sessions = db.trainingSessionDao().getSessionsForUser(currentUserId);
            int sessionId = sessions.get(0).id;

            for (ExerciseRecord record : exerciseRecords) {
                record.sessionId = sessionId;
                db.exerciseRecordDao().insertExerciseRecord(record);
            }

            runOnUiThread(() -> {
                Toast.makeText(AddTrainingSessionActivity.this, "Training session saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
