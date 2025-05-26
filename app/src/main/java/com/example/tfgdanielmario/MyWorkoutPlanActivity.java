package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MyWorkoutPlanActivity extends AppCompatActivity {

    private static final int ADD_TRAINING_SESSION_REQUEST = 1001;
    private RecyclerView recyclerView;
    private WorkoutPlanAdapter adapter;
    private List<TrainingSession> trainingSessions = new ArrayList<>();
    private Button btnNewPlan, btnEdit;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myworkout_plan);

        // Configurar la barra de navegación
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_workout);
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_progress) {
                startActivity(new Intent(this, ProgressActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, Profile.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_workout) {
                return true;
            }
            return false;
        });

        // Configurar botón de información
        findViewById(R.id.btnInfo).setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage(getString(R.string.reps_info))
                .setPositiveButton(getString(R.string.ok), null)
                .show();
        });

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.rvWorkoutPlan);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new WorkoutPlanAdapter(this, trainingSessions, session -> {
            Intent intent = new Intent(MyWorkoutPlanActivity.this, TrainingSessionActivity.class);
            intent.putExtra("sessionId", session.getId());
            intent.putExtra("sessionName", session.getName());
            int currentDay = trainingSessions.indexOf(session) + 1;
            int totalDays = trainingSessions.size();
            intent.putExtra("currentDay", currentDay);
            intent.putExtra("totalDays", totalDays);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        btnNewPlan = findViewById(R.id.btnNewPlan);
        btnEdit = findViewById(R.id.btnEditPlan);
        btnNewPlan.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTrainingSessionActivity.class);
            startActivityForResult(intent, ADD_TRAINING_SESSION_REQUEST);
        });
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditPlanListActivity.class);
            startActivity(intent);
        });

        loadTrainingSessions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TRAINING_SESSION_REQUEST && resultCode == RESULT_OK && data != null) {
            String sessionId = data.getStringExtra("sessionId");
            String sessionName = data.getStringExtra("sessionName");
            if (sessionId != null && sessionName != null) {
                Toast.makeText(this, getString(R.string.routine_created, sessionName), Toast.LENGTH_SHORT).show();
                loadTrainingSessions();
            }
        }
    }

    private void loadTrainingSessions() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e("MyWorkoutPlanActivity", "No user logged in");
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("trainingSessions")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    trainingSessions.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        TrainingSession session = doc.toObject(TrainingSession.class);
                        if (session != null) {
                            session.setId(doc.getId());
                            trainingSessions.add(session);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading training sessions", e));
    }
}
