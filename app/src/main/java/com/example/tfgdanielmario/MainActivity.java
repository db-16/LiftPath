package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Cargar el fragmento inicial (Workout)
        loadWorkoutActivity();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.navigation_workout) {
            loadWorkoutActivity();
            return true;
        } else if (item.getItemId() == R.id.navigation_progress) {
            Intent intent = new Intent(this, ProgressActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.navigation_profile) {
            Intent intent = new Intent(this, Profile.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    private void loadWorkoutActivity() {
        Intent intent = new Intent(this, MyWorkoutPlanActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurarse de que el ítem correcto esté seleccionado
        updateSelectedNavigationItem();
    }

    private void updateSelectedNavigationItem() {
        Class<?> currentActivity = getClass();
        if (currentActivity == MyWorkoutPlanActivity.class) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_workout);
        } else if (currentActivity == ProgressActivity.class) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_progress);
        } else if (currentActivity == Profile.class) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        }
    }
}