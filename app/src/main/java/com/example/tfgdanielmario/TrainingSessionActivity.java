package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrainingSessionActivity extends AppCompatActivity {
    private static final String TAG = "TrainingSession";

    private RecyclerView rvExercises;
    private ExerciseProgressAdapter adapter;
    private List<ExerciseRecord> exercises = new ArrayList<>();
    private FirebaseFirestore db;
    private String sessionId;
    private String userId;
    private Button saveTraining;
    private LoadEstimator loadEstimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_session);

        // Habilitar el botón de retroceso en la ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Entrenamiento");
        }

        rvExercises = findViewById(R.id.rvExercises);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Usuario no logueado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Obtener sessionId y nombre de la sesión
        sessionId = getIntent().getStringExtra("sessionId");
        String sessionName = getIntent().getStringExtra("sessionName");
        
        if (sessionId == null) {
            Toast.makeText(this, "Error: ID de sesión no disponible", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Establecer el nombre de la sesión en el título
        TextView tvSessionName = findViewById(R.id.tvSessionName);
        if (sessionName != null) {
            tvSessionName.setText(sessionName);
        }

        loadEstimator = new LoadEstimator(this, userId);

        saveTraining = findViewById(R.id.btnSaveTraining);
        saveTraining.setOnClickListener(v -> saveTrainingSession());

        // Crear el adaptador primero
        adapter = new ExerciseProgressAdapter(exercises, exercise -> {
            ExerciseProgressDialogFragment dialog = ExerciseProgressDialogFragment.newInstance(exercise);
            dialog.setOnProgressSavedListener(updatedExercise -> {
                if (updatedExercise.getId() == null || updatedExercise.getId().isEmpty()) {
                    updatedExercise.setId(UUID.randomUUID().toString());
                }
                updatedExercise.setUserId(userId);

                boolean found = false;
                for (int i = 0; i < exercises.size(); i++) {
                    ExerciseRecord e = exercises.get(i);
                    if (e.getId() != null && e.getId().equals(updatedExercise.getId())) {
                        exercises.set(i, updatedExercise);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    exercises.add(updatedExercise);
                }

                db.collection("exerciseRecords")
                        .document(updatedExercise.getId())
                        .set(updatedExercise)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(this, "Progreso guardado", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error al guardar el progreso", Toast.LENGTH_SHORT).show());

                adapter.notifyDataSetChanged();
            });
            dialog.show(getSupportFragmentManager(), "ExerciseProgressDialog");
        });

        rvExercises.setAdapter(adapter);

        // Cargar ejercicios
        loadExercises();
    }

    private void loadExercises() {
        Log.d(TAG, "Cargando ejercicios para sesión: " + sessionId);
        
        db.collection("trainingSessions")
                .document(sessionId)
                .collection("exercises")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    exercises.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ExerciseRecord record = doc.toObject(ExerciseRecord.class);
                        if (record != null) {
                            record.setId(doc.getId());
                            record.setProgress(new ArrayList<>());
                            exercises.add(record);
                            Log.d(TAG, "Ejercicio cargado: " + record.getExerciseName());
                        }
                    }
                    
                    if (exercises.isEmpty()) {
                        Log.d(TAG, "No se encontraron ejercicios en la sesión");
                        Toast.makeText(this, "No hay ejercicios en esta rutina", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Ejercicios cargados: " + exercises.size());
                        // Cargar el historial y estimar las cargas
                        loadHistoryAndEstimateLoads();
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar ejercicios: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar ejercicios del plan", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadHistoryAndEstimateLoads() {
        // Cargar el historial más reciente para esta sesión
        db.collection("trainingHistory")
                .whereEqualTo("sessionId", sessionId)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(historySnapshots -> {
                    if (!historySnapshots.isEmpty()) {
                        // Obtener el último historial
                        TrainingHistory lastHistory = historySnapshots.getDocuments().get(0).toObject(TrainingHistory.class);
                        if (lastHistory != null && lastHistory.getExercises() != null) {
                            // Actualizar los pesos iniciales con los últimos pesos usados
                            for (ExerciseRecord historicExercise : lastHistory.getExercises()) {
                                for (ExerciseRecord currentExercise : exercises) {
                                    if (currentExercise.getExerciseName().equals(historicExercise.getExerciseName()) &&
                                        historicExercise.getProgress() != null && 
                                        !historicExercise.getProgress().isEmpty()) {
                                        // Encontrar el peso más alto del último entrenamiento
                                        double maxWeight = historicExercise.getProgress().get(0).getWeight();
                                        for (ExerciseProgress progress : historicExercise.getProgress()) {
                                            if (progress.getWeight() > maxWeight) {
                                                maxWeight = progress.getWeight();
                                            }
                                        }
                                        currentExercise.setInitialWeight((float)maxWeight);
                                    }
                                }
                            }
                        }
                    }
                    
                    // Después de cargar el historial, estimar las cargas
                    loadEstimator.estimateLoadsForSession(sessionId, estimatedExercises -> {
                        for (ExerciseRecord estimatedExercise : estimatedExercises) {
                            for (ExerciseRecord currentExercise : exercises) {
                                if (currentExercise.getId().equals(estimatedExercise.getId())) {
                                    currentExercise.setEstimatedWeight(estimatedExercise.getEstimatedWeight());
                                    break;
                                }
                            }
                        }
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar historial: " + e.getMessage());
                    // Si falla la carga del historial, al menos intentar estimar las cargas
                    loadEstimator.estimateLoadsForSession(sessionId, estimatedExercises -> {
                        for (ExerciseRecord estimatedExercise : estimatedExercises) {
                            for (ExerciseRecord currentExercise : exercises) {
                                if (currentExercise.getId().equals(estimatedExercise.getId())) {
                                    currentExercise.setEstimatedWeight(estimatedExercise.getEstimatedWeight());
                                    break;
                                }
                            }
                        }
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    });
                });
    }

    private void saveTrainingSession() {
        if (exercises.isEmpty()) {
            Toast.makeText(this, "No hay ejercicios guardados", Toast.LENGTH_SHORT).show();
            return;
        }

        String historyId = UUID.randomUUID().toString();
        TrainingHistory history = new TrainingHistory(
                historyId,
                userId,
                sessionId,
                new java.util.Date(),
                new ArrayList<>(exercises)
        );

        // Primero guardamos en el historial
        db.collection("trainingHistory")
                .document(historyId)
                .set(history)
                .addOnSuccessListener(aVoid -> {
                    // Después de guardar el historial, actualizamos los ejercicios en la sesión
                    WriteBatch batch = db.batch();
                    
                    // Primero eliminamos los ejercicios existentes
                    db.collection("trainingSessions")
                            .document(sessionId)
                            .collection("exercises")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                // Eliminar ejercicios existentes
                                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                    batch.delete(doc.getReference());
                                }
                                
                                // Añadir los ejercicios actualizados
                                for (ExerciseRecord exercise : exercises) {
                                    // Actualizar el peso inicial con el último peso más alto usado
                                    if (exercise.getProgress() != null && !exercise.getProgress().isEmpty()) {
                                        double maxWeight = exercise.getProgress().get(0).getWeight();
                                        for (ExerciseProgress progress : exercise.getProgress()) {
                                            if (progress.getWeight() > maxWeight) {
                                                maxWeight = progress.getWeight();
                                            }
                                        }
                                        exercise.setInitialWeight((float)maxWeight);
                                    }
                                    
                                    // Limpiar el progreso para la próxima sesión
                                    exercise.setProgress(new ArrayList<>());
                                    
                                    // Añadir el ejercicio actualizado
                                    batch.set(
                                        db.collection("trainingSessions")
                                            .document(sessionId)
                                            .collection("exercises")
                                            .document(),
                                        exercise
                                    );
                                }
                                
                                // Ejecutar todas las operaciones
                                batch.commit()
                                    .addOnSuccessListener(batchResult -> {
                                        Toast.makeText(this, "Entrenamiento guardado correctamente", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(TrainingSessionActivity.this, MyWorkoutPlanActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error al actualizar ejercicios en la sesión", e);
                                        Toast.makeText(this, "Error al actualizar ejercicios", Toast.LENGTH_SHORT).show();
                                    });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al obtener ejercicios existentes", e);
                                Toast.makeText(this, "Error al actualizar ejercicios", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar el historial", e);
                    Toast.makeText(this, "Error al guardar el historial", Toast.LENGTH_SHORT).show();
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
        // Si hay progreso guardado, mostrar diálogo de confirmación
        boolean hasProgress = false;
        for (ExerciseRecord exercise : exercises) {
            if (exercise.getProgress() != null && !exercise.getProgress().isEmpty()) {
                hasProgress = true;
                break;
            }
        }

        if (hasProgress) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("¿Salir sin guardar?")
                .setMessage("Tienes progreso sin guardar. ¿Estás seguro de que quieres salir?")
                .setPositiveButton("Salir", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Cancelar", null)
                .show();
        } else {
            super.onBackPressed();
        }
    }
}
