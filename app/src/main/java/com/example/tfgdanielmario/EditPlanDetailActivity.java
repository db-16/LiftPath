package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EditPlanDetailActivity extends AppCompatActivity {

    private static final String TAG = "EditPlanDetail";
    private static final int ADD_EXERCISE_REQUEST_CODE = 1001;

    private EditText etSessionName;
    private RecyclerView rvExercises;
    private Button btnAddExercise, btnSaveChanges, btnDeletePlan;
    private ExerciseAdapter adapter;
    private List<ExerciseRecord> exercises;
    private FirebaseFirestore db;
    private String sessionId;
    private String sessionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plan_detail);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Editar Rutina");
        }

        // Obtener ID de la sesión
        sessionId = getIntent().getStringExtra("sessionId");
        if (sessionId == null) {
            Toast.makeText(this, "Error: No se pudo cargar la rutina", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas y variables
        etSessionName = findViewById(R.id.etSessionName);
        rvExercises = findViewById(R.id.rvExercises);
        btnAddExercise = findViewById(R.id.btnAddExercise);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnDeletePlan = findViewById(R.id.btnDeletePlan);
        exercises = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        // Configurar RecyclerView
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseAdapter(exercises, new ExerciseAdapter.OnItemActionListener() {
            @Override
            public void onEdit(ExerciseRecord exercise) {
                editExercise(exercise);
            }

            @Override
            public void onDelete(ExerciseRecord exercise) {
                deleteExercise(exercise);
            }
        });
        rvExercises.setAdapter(adapter);

        // Configurar botones
        btnAddExercise.setOnClickListener(v -> {
            AddExerciseDialogFragment dialog = new AddExerciseDialogFragment();
            dialog.setOnExerciseAddedListener(exercise -> {
                exercise.setSessionId(sessionId);
                // Añadir nuevo ejercicio
                db.collection("trainingSessions")
                        .document(sessionId)
                        .collection("exercises")
                        .add(exercise)
                        .addOnSuccessListener(documentReference -> {
                            exercise.setId(documentReference.getId());
                            exercises.add(exercise);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(this, getString(R.string.exercise_added), Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al añadir ejercicio", e);
                            Toast.makeText(this, getString(R.string.error_adding_exercise), Toast.LENGTH_SHORT).show();
                        });
            });
            dialog.show(getSupportFragmentManager(), "AddExerciseDialog");
        });

        btnSaveChanges.setOnClickListener(v -> saveChanges());
        btnDeletePlan.setOnClickListener(v -> confirmDelete());

        // Cargar datos
        loadSessionData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadSessionData() {
        // Cargar datos de la sesión
        db.collection("trainingSessions")
                .document(sessionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    TrainingSession session = documentSnapshot.toObject(TrainingSession.class);
                    if (session != null) {
                        sessionName = session.getName();
                        etSessionName.setText(sessionName);
                        loadExercises();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar la sesión", e);
                    Toast.makeText(this, "Error al cargar la sesión", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadExercises() {
        db.collection("trainingSessions")
                .document(sessionId)
                .collection("exercises")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    exercises.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        ExerciseRecord exercise = doc.toObject(ExerciseRecord.class);
                        if (exercise != null) {
                            exercise.setId(doc.getId());
                            exercises.add(exercise);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar ejercicios", e);
                    Toast.makeText(this, "Error al cargar ejercicios", Toast.LENGTH_SHORT).show();
                });
    }

    private void editExercise(ExerciseRecord exercise) {
        EditExerciseDialogFragment dialog = EditExerciseDialogFragment.newInstance(exercise);
        dialog.setOnExerciseEditedListener(updatedExercise -> {
            // Actualizar ejercicio existente
            db.collection("trainingSessions")
                    .document(sessionId)
                    .collection("exercises")
                    .document(exercise.getId())
                    .set(updatedExercise)
                    .addOnSuccessListener(aVoid -> {
                        // Actualizar el ejercicio en la lista local
                        for (int i = 0; i < exercises.size(); i++) {
                            if (exercises.get(i).getId().equals(exercise.getId())) {
                                exercises.set(i, updatedExercise);
                                adapter.notifyItemChanged(i);
                                break;
                            }
                        }
                        Toast.makeText(this, getString(R.string.exercise_updated), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al actualizar ejercicio", e);
                        Toast.makeText(this, getString(R.string.error_updating_exercise), Toast.LENGTH_SHORT).show();
                    });
        });
        dialog.show(getSupportFragmentManager(), "EditExerciseDialog");
    }

    private void deleteExercise(ExerciseRecord exercise) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_exercise_title))
                .setMessage(getString(R.string.delete_exercise_message))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    db.collection("trainingSessions")
                            .document(sessionId)
                            .collection("exercises")
                            .document(exercise.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                exercises.remove(exercise);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(this, getString(R.string.exercise_deleted), Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, 
                                getString(R.string.error_deleting_exercise), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void saveChanges() {
        String newName = etSessionName.getText().toString().trim();
        if (newName.isEmpty()) {
            etSessionName.setError(getString(R.string.name_cannot_be_empty));
            return;
        }

        btnSaveChanges.setEnabled(false);
        
        db.collection("trainingSessions")
                .document(sessionId)
                .update("name", newName)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.changes_saved), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MyWorkoutPlanActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar cambios", e);
                    Toast.makeText(this, getString(R.string.error_saving_changes), Toast.LENGTH_SHORT).show();
                    btnSaveChanges.setEnabled(true);
                });
    }

    private void confirmDelete() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_routine_title))
                .setMessage(getString(R.string.delete_routine_message))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> deletePlan())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void deletePlan() {
        db.collection("trainingSessions")
                .document(sessionId)
                .collection("exercises")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    // Añadir eliminación de ejercicios al batch
                    querySnapshot.getDocuments().forEach(doc -> 
                        batch.delete(doc.getReference()));
                    
                    // Añadir eliminación de la sesión al batch
                    batch.delete(db.collection("trainingSessions").document(sessionId));

                    batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Rutina eliminada", Toast.LENGTH_SHORT).show();
                            // Navegar a MyWorkoutPlanActivity y limpiar la pila de actividades
                            Intent intent = new Intent(this, MyWorkoutPlanActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al eliminar rutina", e);
                            Toast.makeText(this, "Error al eliminar rutina", Toast.LENGTH_SHORT).show();
                        });
                });
    }
}
