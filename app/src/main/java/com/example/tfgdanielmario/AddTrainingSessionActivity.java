package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class AddTrainingSessionActivity extends AppCompatActivity {

    private static final String TAG = "AddTrainingSession";
    private static final int ADD_EXERCISE_REQUEST_CODE = 1001;

    private EditText etSessionName;
    private Button btnAddExercise;
    private Button btnSaveSession;
    private LinearLayout layoutExercises;
    private List<ExerciseRecord> exercises;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_training_session);

        // Habilitar el botón de retroceso
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nueva Rutina");
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
        btnAddExercise.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddExercise.class);
            startActivityForResult(intent, ADD_EXERCISE_REQUEST_CODE);
        });

        btnSaveSession.setOnClickListener(v -> saveSession());

        // Actualizar UI
        updateUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == ADD_EXERCISE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ExerciseRecord exercise = (ExerciseRecord) data.getSerializableExtra("exercise");
            if (exercise != null) {
                exercises.add(exercise);
                updateUI();
                Toast.makeText(this, "Ejercicio añadido", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUI() {
        // Actualizar botón de guardar
        btnSaveSession.setEnabled(!exercises.isEmpty());

        // Actualizar lista de ejercicios
        layoutExercises.removeAllViews();
        for (int i = 0; i < exercises.size(); i++) {
            ExerciseRecord exercise = exercises.get(i);
            View exerciseView = getLayoutInflater().inflate(R.layout.item_exercise, layoutExercises, false);

            TextView tvName = exerciseView.findViewById(R.id.tvNameExersice);
            View btnDelete = exerciseView.findViewById(R.id.ivDelete);

            String displayText = String.format("%d. %s - %d series (%.1f kg)", 
                i + 1, 
                exercise.getExerciseName(), 
                exercise.getSets(), 
                exercise.getInitialWeight());
            tvName.setText(displayText);

            final int position = i;
            btnDelete.setOnClickListener(v -> {
                exercises.remove(position);
                updateUI();
                Toast.makeText(this, "Ejercicio eliminado", Toast.LENGTH_SHORT).show();
            });

            layoutExercises.addView(exerciseView);
        }
    }

    private void saveSession() {
        String sessionName = etSessionName.getText().toString().trim();
        if (sessionName.isEmpty()) {
            etSessionName.setError("Ingresa un nombre para la rutina");
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
                            Toast.makeText(this, "Rutina guardada correctamente", Toast.LENGTH_SHORT).show();
                            
                            // Devolver resultado
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("sessionId", sessionId);
                            resultIntent.putExtra("sessionName", sessionName);
                            setResult(RESULT_OK, resultIntent);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!exercises.isEmpty()) {
            // Mostrar diálogo de confirmación si hay ejercicios
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("¿Descartar cambios?")
                .setMessage("Tienes ejercicios sin guardar. ¿Deseas descartarlos?")
                .setPositiveButton("Descartar", (dialog, which) -> {
                    setResult(RESULT_CANCELED);
                    super.onBackPressed();
                })
                .setNegativeButton("Cancelar", null)
                .show();
        } else {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }
}
