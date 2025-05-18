package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EditPlanListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WorkoutPlanAdapter adapter;
    private List<TrainingSession> trainingSessions = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plan_list);

        recyclerView = findViewById(R.id.rvWorkoutPlan);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new WorkoutPlanAdapter(this, trainingSessions, session -> {
            Intent intent = new Intent(EditPlanListActivity.this, EditPlanDetailActivity.class);
            intent.putExtra("sessionId", session.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        loadUserPlans();
    }

    private void loadUserPlans() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("trainingSessions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    trainingSessions.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        TrainingSession session = doc.toObject(TrainingSession.class);
                        if (session != null) {
                            session.setId(doc.getId());
                            trainingSessions.add(session);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}

