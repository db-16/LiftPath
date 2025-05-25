package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Profile extends AppCompatActivity {

    private TextView tvNameAge, tvCurrentWeight, tvGoal, tvTrainingCount, tvDailyCalories;
    private Button btnSettings, btnLogout;
    private String currentUserMail;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Configurar la barra de navegación
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_progress) {
                Intent progressIntent = new Intent(Profile.this, ProgressActivity.class);
                startActivity(progressIntent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_workout) {
                Intent workoutIntent = new Intent(Profile.this, MyWorkoutPlanActivity.class);
                startActivity(workoutIntent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                return true;
            }
            return false;
        });

        // Inicializamos Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Vinculamos los componentes de la UI
        tvNameAge = findViewById(R.id.tvNameAge);
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvGoal = findViewById(R.id.tvGoal);
        tvTrainingCount = findViewById(R.id.tvTrainingCount);
        tvDailyCalories = findViewById(R.id.tvExerciseCount);
        btnSettings = findViewById(R.id.btnSettings);
        btnLogout = findViewById(R.id.btnLogout);

        currentUserMail = mAuth.getCurrentUser().getEmail();

        // Cargar los datos del usuario
        loadUserData();
        loadTrainingStats();

        // Configurar botones
        btnSettings.setOnClickListener(view -> showSettingsDialog());
        btnLogout.setOnClickListener(view -> logout());
    }

    private void loadUserData() {
        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            tvNameAge.setText(getString(R.string.name_age, user.getName(), user.getAge()));
                            tvCurrentWeight.setText(getString(R.string.weight_format, user.getCurrentWeight()));
                            
                            // Calcular la diferencia con el objetivo
                            double difference = user.getGoalWeight() - user.getCurrentWeight();
                            String goalMessage;
                            if (Math.abs(difference) < 0.1) { // Consideramos objetivo cumplido si la diferencia es menor a 0.1 kg
                                goalMessage = getString(R.string.goal_achieved);
                                tvGoal.setTextColor(getResources().getColor(R.color.goal_achieved));
                            } else if (difference > 0) {
                                goalMessage = getString(R.string.weight_difference_positive, difference);
                                tvGoal.setTextColor(getResources().getColor(R.color.white));
                            } else {
                                goalMessage = getString(R.string.weight_difference_negative, difference);
                                tvGoal.setTextColor(getResources().getColor(R.color.white));
                            }
                            tvGoal.setText(goalMessage);

                            // Mostrar calorías diarias
                            tvDailyCalories.setText(getString(R.string.calories_per_day, user.getDailyCalories()));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Profile.this, getString(R.string.loading_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadTrainingStats() {
        String userId = mAuth.getCurrentUser().getUid();
        
        // Contar entrenamientos
        db.collection("trainingSessions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(trainingSessions -> {
                tvTrainingCount.setText(String.valueOf(trainingSessions.size()));
            })
            .addOnFailureListener(e -> {
                tvTrainingCount.setText("0");
            });
    }

    private void showSettingsDialog() {
        SettingsDialogFragment dialog = SettingsDialogFragment.newInstance();
        dialog.setOnSettingsSavedListener(() -> {
            loadUserData();
            loadTrainingStats();
        });
        dialog.show(getSupportFragmentManager(), "settings");
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(Profile.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
