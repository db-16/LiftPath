package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyWorkoutPlanActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WorkoutPlanAdapter adapter;
    private List<WorkoutDay> workoutDays = new ArrayList<>();
    private Button btnNewPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myworkout_plan);

        recyclerView = findViewById(R.id.rvWorkoutPlan);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new WorkoutPlanAdapter(workoutDays);
        recyclerView.setAdapter(adapter);

        btnNewPlan = findViewById(R.id.btnNewPlan);
        btnNewPlan.setOnClickListener(v -> addNewDay());
    }

    public Intent addNewDay() {
        Intent intent = new Intent(this, NewPlan.class);
        return intent;
    }

}
