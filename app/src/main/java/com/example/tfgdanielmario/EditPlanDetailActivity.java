package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

        // Configurar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Editar Rutina");
        }

        // Inicializar vistas
        etSessionName = findViewById(R.id.etSessionName);
        rvExercises = findViewById(R.id.rvExercises);
        btnAddExercise = findViewById(R.id.btnAddExercise);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnDeletePlan = findViewById(R.id.btnDeletePlan);

        // Inicializar Firebase y lista de ejercicios
        db = FirebaseFirestore.getInstance();
        exercises = new ArrayList<>();

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

        // Obtener ID de la sesión
        sessionId = getIntent().getStringExtra("sessionId");
        if (sessionId == null) {
            Toast.makeText(this, "Error: No se pudo cargar la rutina", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar botones
        btnAddExercise.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddExercise.class);
            startActivityForResult(intent, ADD_EXERCISE_REQUEST_CODE);
        });

        btnSaveChanges.setOnClickListener(v -> saveChanges());
        btnDeletePlan.setOnClickListener(v -> confirmDelete());

        // Cargar datos
        loadSessionData();
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
        Intent intent = new Intent(this, EditExerciseActivity.class);
        intent.putExtra("exerciseRecord", exercise);
        startActivityForResult(intent, ADD_EXERCISE_REQUEST_CODE);
    }

    private void deleteExercise(ExerciseRecord exercise) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar ejercicio")
                .setMessage("¿Estás seguro de que quieres eliminar este ejercicio?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("trainingSessions")
                            .document(sessionId)
                            .collection("exercises")
                            .document(exercise.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                exercises.remove(exercise);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(this, "Ejercicio eliminado", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, 
                                "Error al eliminar ejercicio", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void saveChanges() {
        String newName = etSessionName.getText().toString().trim();
        if (newName.isEmpty()) {
            etSessionName.setError("El nombre no puede estar vacío");
            return;
        }

        btnSaveChanges.setEnabled(false);
        
        db.collection("trainingSessions")
                .document(sessionId)
                .update("name", newName)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show();
                    // Navegar a MyWorkoutPlanActivity y limpiar la pila de actividades
                    Intent intent = new Intent(this, MyWorkoutPlanActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar cambios", e);
                    Toast.makeText(this, "Error al guardar cambios", Toast.LENGTH_SHORT).show();
                    btnSaveChanges.setEnabled(true);
                });
    }

    private void confirmDelete() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar rutina")
                .setMessage("¿Estás seguro de que quieres eliminar esta rutina?")
                .setPositiveButton("Eliminar", (dialog, which) -> deletePlan())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deletePlan() {
        // Primero eliminar todos los ejercicios
        db.collection("trainingSessions")
                .document(sessionId)
                .collection("exercises")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Usar batch para eliminar todo de forma atómica
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    
                    // Añadir eliminación de ejercicios al batch
                    querySnapshot.getDocuments().forEach(doc -> 
                        batch.delete(doc.getReference()));
                    
                    // Añadir eliminación de la sesión al batch
                    batch.delete(db.collection("trainingSessions").document(sessionId));
                    
                    // Ejecutar el batch
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_EXERCISE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ExerciseRecord exercise = (ExerciseRecord) data.getSerializableExtra("exercise");
            if (exercise != null) {
                if (exercise.getId() != null) {
                    // Actualizar ejercicio existente
                    db.collection("trainingSessions")
                            .document(sessionId)
                            .collection("exercises")
                            .document(exercise.getId())
                            .set(exercise)
                            .addOnSuccessListener(aVoid -> {
                                // Actualizar el ejercicio en la lista local
                                for (int i = 0; i < exercises.size(); i++) {
                                    if (exercises.get(i).getId().equals(exercise.getId())) {
                                        exercises.set(i, exercise);
                                        adapter.notifyItemChanged(i);
                                        break;
                                    }
                                }
                                Toast.makeText(this, "Ejercicio actualizado", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al actualizar ejercicio", e);
                                Toast.makeText(this, "Error al actualizar ejercicio", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Añadir nuevo ejercicio
                    exercise.setSessionId(sessionId);
                    db.collection("trainingSessions")
                            .document(sessionId)
                            .collection("exercises")
                            .add(exercise)
                            .addOnSuccessListener(documentReference -> {
                                exercise.setId(documentReference.getId());
                                exercises.add(exercise);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(this, "Ejercicio añadido", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al añadir ejercicio", e);
                                Toast.makeText(this, "Error al añadir ejercicio", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
