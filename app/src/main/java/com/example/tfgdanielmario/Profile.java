package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Profile extends AppCompatActivity {

    private TextView tvNameAge, tvCurrentWeight, tvGoal;
    private EditText etNewWeight;
    private Button btnSaveWeight, btnSettings, btnLogout;
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
        etNewWeight = findViewById(R.id.etNewWeight);
        btnSaveWeight = findViewById(R.id.btnSaveWeight);
        btnSettings = findViewById(R.id.btnSettings);
        btnLogout = findViewById(R.id.btnLogout);

        currentUserMail = mAuth.getCurrentUser().getEmail();

        // Cargar los datos del usuario
        loadUserData();

        // Configurar botones
        btnSaveWeight.setOnClickListener(view -> saveNewWeight());
        btnSettings.setOnClickListener(view -> showSettingsDialog());
        btnLogout.setOnClickListener(view -> logout());
    }

    private void loadUserData() {
        db.collection("users").document(currentUserMail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            tvNameAge.setText(user.getName());
                            tvCurrentWeight.setText("Peso actual: " + user.getWeight() + " kg");
                            tvGoal.setText("Objetivo: " + user.getGoalWeight() + " kg (" + user.getGoalType() + ")");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Profile.this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSettingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        EditText etCurrentWeight = dialogView.findViewById(R.id.etCurrentWeight);
        EditText etGoalWeight = dialogView.findViewById(R.id.etGoalWeight);
        Spinner spinnerGoalType = dialogView.findViewById(R.id.spinnerGoalType);

        // Configurar el spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.goal_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoalType.setAdapter(adapter);

        // Cargar datos actuales
        db.collection("users").document(currentUserMail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            etCurrentWeight.setText(String.valueOf(user.getWeight()));
                            etGoalWeight.setText(String.valueOf(user.getGoalWeight()));
                            // Establecer la selección del spinner según el tipo de objetivo actual
                            String[] goalTypes = getResources().getStringArray(R.array.goal_types);
                            for (int i = 0; i < goalTypes.length; i++) {
                                if (goalTypes[i].equals(user.getGoalType())) {
                                    spinnerGoalType.setSelection(i);
                                    break;
                                }
                            }
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
                        String goalType = spinnerGoalType.getSelectedItem().toString();

                        // Actualizar en Firestore
                        db.collection("users").document(currentUserMail)
                                .update(
                                        "weight", currentWeight,
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

    private void saveNewWeight() {
        String newWeightStr = etNewWeight.getText().toString().trim();
        if (newWeightStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa un peso", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double newWeight = Double.parseDouble(newWeightStr);
            db.collection("users").document(currentUserMail)
                    .update("weight", newWeight)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Profile.this, "Peso actualizado", Toast.LENGTH_SHORT).show();
                        loadUserData();
                        etNewWeight.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Profile.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor ingresa un peso válido", Toast.LENGTH_SHORT).show();
        }
    }
}
