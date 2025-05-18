package com.example.tfgdanielmario;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LoadEstimator {
    private static final String TAG = "LoadEstimator";
    private final FirebaseFirestore db;
    private final String userId;

    public LoadEstimator(Context context, String userId) {
        this.db = FirebaseFirestore.getInstance();
        this.userId = userId;
    }

    public void estimateLoadsForSession(String sessionId, Consumer<List<ExerciseRecord>> callback) {
        Log.d(TAG, "Iniciando estimación de cargas para sesión: " + sessionId + ", usuario: " + userId);
        
        // Primero obtener el objetivo del usuario
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String goal = userDoc.getString("goal");
                    if (goal == null) {
                        Log.e(TAG, "Objetivo del usuario no encontrado para userId: " + userId);
                        callback.accept(new ArrayList<>());
                        return;
                    }
                    Log.d(TAG, "Objetivo del usuario encontrado: " + goal);

                    // Calcular factor de ajuste basado en el objetivo
                    float adjustmentFactor;
                    String goalLower = goal.toLowerCase();
                    if (goalLower.equals("gain")) {
                        adjustmentFactor = 1.05f;    // Incremento del 5% para ganar masa
                    } else if (goalLower.equals("maintain")) {
                        adjustmentFactor = 1.0f;     // Mantener el peso actual
                    } else if (goalLower.equals("lose")) {
                        adjustmentFactor = 0.95f;    // Reducción del 5% para pérdida
                    } else {
                        adjustmentFactor = 1.0f;
                    }
                    Log.d(TAG, String.format("Factor de ajuste calculado: %.2f basado en objetivo: %s", adjustmentFactor, goal));

                    // Luego obtener los ejercicios de la sesión
                    db.collection("trainingSessions")
                            .document(sessionId)
                            .collection("exercises")
                            .get()
                            .addOnSuccessListener(exerciseSnapshots -> {
                                List<ExerciseRecord> estimatedRecords = new ArrayList<>();
                                final int totalExercises = exerciseSnapshots.size();
                                Log.d(TAG, "Encontrados " + totalExercises + " ejercicios en la sesión");
                                
                                if (totalExercises == 0) {
                                    Log.w(TAG, "No se encontraron ejercicios en la sesión");
                                    callback.accept(estimatedRecords);
                                    return;
                                }

                                // Para cada ejercicio
                                for (QueryDocumentSnapshot exerciseDoc : exerciseSnapshots) {
                                    ExerciseRecord record = exerciseDoc.toObject(ExerciseRecord.class);
                                    if (record != null) {
                                        record.setId(exerciseDoc.getId());
                                        String exerciseName = record.getExerciseName();
                                        Log.d(TAG, "===== Procesando ejercicio: " + exerciseName + " =====");
                                        Log.d(TAG, "Peso inicial del ejercicio: " + record.getInitialWeight());
                                        
                                        // Buscar el historial específico de este ejercicio
                                        db.collection("trainingHistory")
                                                .whereEqualTo("userId", userId)
                                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                                .get()
                                                .addOnSuccessListener(historySnapshots -> {
                                                    Log.d(TAG, "Encontrados " + historySnapshots.size() + " registros de historial para el usuario");
                                                    double lastWeight = record.getInitialWeight();
                                                    double maxWeight = record.getInitialWeight();
                                                    boolean foundHistory = false;

                                                    // Buscar en el historial el último peso y el peso máximo
                                                    for (QueryDocumentSnapshot historyDoc : historySnapshots) {
                                                        TrainingHistory history = historyDoc.toObject(TrainingHistory.class);
                                                        Log.d(TAG, "Revisando historial con ID: " + historyDoc.getId() + ", fecha: " + history.getTimestamp());
                                                        
                                                        if (history != null && history.getExercises() != null) {
                                                            for (ExerciseRecord historicExercise : history.getExercises()) {
                                                                if (historicExercise.getExerciseName().equals(exerciseName)) {
                                                                    List<ExerciseProgress> progress = historicExercise.getProgress();
                                                                    if (progress != null && !progress.isEmpty()) {
                                                                        foundHistory = true;
                                                                        Log.d(TAG, "Encontrado historial para " + exerciseName + " con " + progress.size() + " series");
                                                                        
                                                                        // Encontrar el peso más alto en este entrenamiento
                                                                        double highestWeightInSession = 0;
                                                                        for (ExerciseProgress p : progress) {
                                                                            if (p.getWeight() > highestWeightInSession) {
                                                                                highestWeightInSession = p.getWeight();
                                                                            }
                                                                        }
                                                                        Log.d(TAG, String.format("Peso más alto en esta sesión histórica: %.1f kg", highestWeightInSession));
                                                                        
                                                                        // Actualizar el último peso y el peso máximo
                                                                        lastWeight = highestWeightInSession;
                                                                        if (highestWeightInSession > maxWeight) {
                                                                            maxWeight = highestWeightInSession;
                                                                        }
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            if (foundHistory) {
                                                                Log.d(TAG, "Se encontró historial relevante, deteniendo búsqueda");
                                                                break;
                                                            }
                                                        }
                                                    }

                                                    // Calcular pesos
                                                    float baseWeight = foundHistory ? (float)lastWeight : record.getInitialWeight();
                                                    float estimatedWeight = baseWeight * adjustmentFactor;
                                                    
                                                    Log.d(TAG, String.format("Resumen para %s:", exerciseName));
                                                    Log.d(TAG, String.format("- Peso base: %.1f kg", baseWeight));
                                                    Log.d(TAG, String.format("- Factor de ajuste: %.2f", adjustmentFactor));
                                                    Log.d(TAG, String.format("- Peso estimado: %.1f kg", estimatedWeight));

                                                    // Establecer los pesos
                                                    record.setInitialWeight((float)lastWeight);
                                                    record.setEstimatedWeight(estimatedWeight);
                                                    estimatedRecords.add(record);

                                                    // Si hemos procesado todos los ejercicios, devolver la lista
                                                    if (estimatedRecords.size() == totalExercises) {
                                                        Log.d(TAG, "Procesamiento completado. Total de ejercicios estimados: " + estimatedRecords.size());
                                                        callback.accept(estimatedRecords);
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error al cargar historial para " + exerciseName + ": " + e.getMessage());
                                                    float baseWeight = record.getInitialWeight();
                                                    float estimatedWeight = baseWeight * adjustmentFactor;
                                                    record.setEstimatedWeight(estimatedWeight);
                                                    estimatedRecords.add(record);
                                                    if (estimatedRecords.size() == totalExercises) {
                                                        callback.accept(estimatedRecords);
                                                    }
                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al cargar ejercicios: " + e.getMessage());
                                callback.accept(new ArrayList<>());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar objetivo del usuario: " + e.getMessage());
                    callback.accept(new ArrayList<>());
                });
    }
}


