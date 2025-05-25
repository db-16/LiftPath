package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

public class AddTrainingSessionActivity extends AppCompatActivity {

    private static final String TAG = "AddTrainingSession";

    private TextInputEditText etSessionName;
    private MaterialButton btnAddExercise;
    private MaterialButton btnSaveSession;
    private LinearLayout layoutExercises;
    private List<ExerciseRecord> exercises;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_training_session);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.add_training);
        }

        // Inicializar vistas
        etSessionName = findViewById(R.id.tvName);
        btnAddExercise = findViewById(R.id.btnAddExercise);
        btnSaveSession = findViewById(R.id.btnSaveSession);
        layoutExercises = findViewById(R.id.exerciseList);

        // Inicializar lista de ejercicios y Firebase
        exercises = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        // Configurar botones
        btnAddExercise.setOnClickListener(v -> showAddExerciseDialog());
        btnSaveSession.setOnClickListener(v -> saveSession());

        // Actualizar UI
        updateUI();
    }

    private void showAddExerciseDialog() {
        AddExerciseDialogFragment dialog = new AddExerciseDialogFragment();
        dialog.setOnExerciseAddedListener(exercise -> {
            exercises.add(exercise);
            updateUI();
            Toast.makeText(this, getString(R.string.exercise_added), Toast.LENGTH_SHORT).show();
        });
        dialog.show(getSupportFragmentManager(), "AddExerciseDialog");
    }

    private void updateUI() {
        // Actualizar botón de guardar
        btnSaveSession.setEnabled(!exercises.isEmpty());

        // Actualizar lista de ejercicios
        layoutExercises.removeAllViews();
        for (ExerciseRecord exercise : exercises) {
            ExerciseItemView itemView = new ExerciseItemView(this);
            itemView.setExercise(exercise);
            itemView.setOnDeleteClickListener(() -> {
                exercises.remove(exercise);
                updateUI();
                Toast.makeText(this, getString(R.string.exercise_deleted), Toast.LENGTH_SHORT).show();
            });
            layoutExercises.addView(itemView);
        }
    }

    private void saveSession() {
        String sessionName = etSessionName.getText().toString().trim();
        if (sessionName.isEmpty()) {
            etSessionName.setError(getString(R.string.fill_all_fields));
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botones mientras se guarda
        btnSaveSession.setEnabled(false);
        btnAddExercise.setEnabled(false);

        // Crear sesión
        TrainingSession session = new TrainingSession(currentUser.getUid(), sessionName);

        // Guardar en Firebase
        db.collection("trainingSessions")
                .add(session)
                .addOnSuccessListener(documentReference -> {
                    String sessionId = documentReference.getId();
                    
                    // Crear batch para ejercicios
                    WriteBatch batch = db.batch();
                    
                    // Añadir ejercicios al batch
                    for (ExerciseRecord exercise : exercises) {
                        exercise.setSessionId(sessionId);
                        exercise.setUserId(currentUser.getUid());
                        batch.set(
                            db.collection("trainingSessions")
                                .document(sessionId)
                                .collection("exercises")
                                .document(),
                            exercise
                        );
                    }

                    // Ejecutar batch
                    batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, getString(R.string.training_saved), Toast.LENGTH_SHORT).show();
                            
                            // Volver a MyWorkoutPlanActivity
                            Intent intent = new Intent(this, MyWorkoutPlanActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al guardar ejercicios", e);
                            Toast.makeText(this, "Error al guardar ejercicios", Toast.LENGTH_SHORT).show();
                            btnSaveSession.setEnabled(true);
                            btnAddExercise.setEnabled(true);
                        });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar sesión", e);
                    Toast.makeText(this, "Error al guardar sesión", Toast.LENGTH_SHORT).show();
                    btnSaveSession.setEnabled(true);
                    btnAddExercise.setEnabled(true);
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!exercises.isEmpty()) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("¿Descartar cambios?")
                .setMessage("Tienes ejercicios sin guardar. ¿Deseas descartarlos?")
                .setPositiveButton("Descartar", (dialog, which) -> {
                    Intent intent = new Intent(this, MyWorkoutPlanActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
        } else {
            Intent intent = new Intent(this, MyWorkoutPlanActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}
