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

    private TextView tvNameAge, tvCurrentWeight, tvGoal, tvTrainingCount, tvExerciseCount;
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
        tvExerciseCount = findViewById(R.id.tvExerciseCount);
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
                            tvNameAge.setText(user.getName());
                            tvCurrentWeight.setText(String.format("%.1f kg", user.getCurrentWeight()));
                            
                            // Calcular la diferencia con el objetivo
                            double difference = user.getGoalWeight() - user.getCurrentWeight();
                            String goalMessage;
                            if (Math.abs(difference) < 0.1) { // Consideramos objetivo cumplido si la diferencia es menor a 0.1 kg
                                goalMessage = "¡Objetivo\ncumplido!";
                                tvGoal.setTextColor(getResources().getColor(R.color.goal_achieved));
                            } else if (difference > 0) {
                                goalMessage = String.format("+%.1f kg", difference);
                                tvGoal.setTextColor(getResources().getColor(R.color.white));
                            } else {
                                goalMessage = String.format("%.1f kg", difference);
                                tvGoal.setTextColor(getResources().getColor(R.color.white));
                            }
                            tvGoal.setText(goalMessage);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Profile.this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

        // Contar ejercicios totales del usuario
        db.collection("exerciseRecords")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(exerciseRecords -> {
                tvExerciseCount.setText(String.valueOf(exerciseRecords.size()));
            })
            .addOnFailureListener(e -> {
                tvExerciseCount.setText("0");
                Toast.makeText(Profile.this, "Error al cargar ejercicios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showSettingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        EditText etCurrentWeight = dialogView.findViewById(R.id.etCurrentWeight);
        EditText etGoalWeight = dialogView.findViewById(R.id.etGoalWeight);

        // Cargar datos actuales
        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            etCurrentWeight.setText(String.valueOf(user.getCurrentWeight()));
                            etGoalWeight.setText(String.valueOf(user.getGoalWeight()));
                        }
                    }
                });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Ajustes")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    try {
                        double currentWeight = Double.parseDouble(etCurrentWeight.getText().toString());
                        double goalWeight = Double.parseDouble(etGoalWeight.getText().toString());

                        // Determinar el tipo de objetivo basado en la comparación de pesos
                        String goalType;
                        if (goalWeight > currentWeight) {
                            goalType = "GAIN";
                        } else if (goalWeight < currentWeight) {
                            goalType = "LOSE";
                        } else {
                            goalType = "MAINTAIN";
                        }

                        // Actualizar en Firestore
                        db.collection("users").document(mAuth.getCurrentUser().getUid())
                                .update(
                                        "currentWeight", currentWeight,
                                        "goalWeight", goalWeight,
                                        "goalType", goalType
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(Profile.this, "Ajustes actualizados", Toast.LENGTH_SHORT).show();
                                    loadUserData();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Profile.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } catch (NumberFormatException e) {
                        Toast.makeText(Profile.this, "Por favor, ingresa valores válidos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void logout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(Profile.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
