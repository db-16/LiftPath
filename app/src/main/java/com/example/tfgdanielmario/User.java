package com.example.tfgdanielmario;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class User {

    private String id;          // UID de Firebase Authentication
    private String name;
    private double weight;      // Peso inicial
    private double currentWeight; // Peso actual
    private double goalWeight;
    private String idRoutine;
    private String mail;
    private String goal; // objetivo como texto
    private String goalType; // "GAIN", "LOSE", "MAINTAIN"
    private int height; // altura en cm
    private String gender; // "MALE" o "FEMALE"
    private int age; // edad en años
    private int dailyCalories; // calorías diarias necesarias

    public User() {}

    public User(String name, String email, double weight, double goalWeight, String goalType) {
        this.name = name;
        this.mail = email;
        this.weight = weight;
        this.currentWeight = weight; // Inicialmente el peso actual es igual al peso inicial
        this.goalWeight = goalWeight;
        this.goalType = goalType;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setCurrentWeight(double currentWeight) {
        this.currentWeight = currentWeight;
    }

    public void setGoalWeight(double goalWeight) {
        this.goalWeight = goalWeight;
    }

    public void setIdRoutine(String idRoutine) {
        this.idRoutine = idRoutine;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    // Nuevos setters
    public void setHeight(int height) {
        this.height = height;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setDailyCalories(int dailyCalories) {
        this.dailyCalories = dailyCalories;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public double getGoalWeight() {
        return goalWeight;
    }

    public String getIdRoutine() {
        return idRoutine;
    }

    public String getMail() {
        return mail;
    }

    public String getGoal() {
        return goal;
    }

    public String getGoalType() {
        return goalType;
    }

    // Nuevos getters
    public int getHeight() {
        return height;
    }

    public String getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public int getDailyCalories() {
        return dailyCalories;
    }

    // Método para calcular las calorías diarias de forma síncrona
    public void calculateDailyCalories() {
        // Calcular TMB (Tasa Metabólica Basal) usando la fórmula de Mifflin-St Jeor
        double tmb;
        if ("MALE".equals(gender)) {
            tmb = (10 * currentWeight) + (6.25 * height) - (5 * age) + 5;
        } else {
            tmb = (10 * currentWeight) + (6.25 * height) - (5 * age) - 161;
        }

        Log.d("CalorieCalculation", "TMB inicial: " + tmb);
        Log.d("CalorieCalculation", "Datos usados - Peso: " + currentWeight + ", Altura: " + height + ", Edad: " + age + ", Género: " + gender);

        // Crear un CountDownLatch para esperar la respuesta de Firebase
        CountDownLatch latch = new CountDownLatch(1);
        final double finalTmb = tmb;

        // Crear un thread para las operaciones de Firebase
        Thread firebaseThread = new Thread(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("trainingSessions")
                    .whereEqualTo("userId", id)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            int numTrainings = queryDocumentSnapshots.size();
                            Log.d("CalorieCalculation", "Número de entrenamientos: " + numTrainings);
                            
                            // Factor de actividad basado en el número de entrenamientos
                            double activityFactor;
                            if (numTrainings <= 1) {
                                activityFactor = 1.3; // Poco activo
                            } else if (numTrainings <= 3) {
                                activityFactor = 1.5; // Moderadamente activo
                            } else if (numTrainings <= 5) {
                                activityFactor = 1.7; // Muy activo
                            } else {
                                activityFactor = 1.9; // Extremadamente activo
                            }

                            // Calcular calorías totales
                            double totalCalories = finalTmb * activityFactor;
                            Log.d("CalorieCalculation", "TMB: " + finalTmb);
                            Log.d("CalorieCalculation", "Factor de actividad: " + activityFactor);
                            Log.d("CalorieCalculation", "Calorías después de factor de actividad: " + totalCalories);

                            // Ajustar según el objetivo
                            if ("LOSE".equals(goalType)) {
                                totalCalories -= 500;
                            } else if ("GAIN".equals(goalType)) {
                                totalCalories += 700;
                            }

                            Log.d("CalorieCalculation", "Calorías finales después de ajuste: " + totalCalories);
                            
                            // Actualizar el valor
                            this.dailyCalories = (int) Math.round(totalCalories);

                            // Actualizar en Firestore
                            CountDownLatch updateLatch = new CountDownLatch(1);
                            db.collection("users")
                                    .document(id)
                                    .update("dailyCalories", this.dailyCalories)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("CalorieCalculation", "Calorías actualizadas en Firestore: " + this.dailyCalories);
                                        updateLatch.countDown();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("CalorieCalculation", "Error actualizando calorías", e);
                                        updateLatch.countDown();
                                    });

                            // Esperar a que se complete la actualización
                            updateLatch.await(0, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } finally {
                            latch.countDown();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("CalorieCalculation", "Error obteniendo entrenamientos", e);
                        // Usar valores por defecto en caso de error
                        double defaultActivityFactor = 1.5;
                        double totalCalories = finalTmb * defaultActivityFactor;
                        
                        if ("LOSE".equals(goalType)) {
                            totalCalories -= 500;
                        } else if ("GAIN".equals(goalType)) {
                            totalCalories += 700;
                        }

                        this.dailyCalories = (int) Math.round(totalCalories);
                        latch.countDown();
                    });
        });

        // Iniciar el thread
        firebaseThread.start();

        try {
            // Esperar a que termine el cálculo (máximo 10 segundos)
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            if (!completed) {
                Log.e("CalorieCalculation", "Timeout esperando el cálculo de calorías");
                // Usar un cálculo por defecto en caso de timeout
                this.dailyCalories = (int) Math.round(tmb * 1.5);
                if ("GAIN".equals(goalType)) {
                    this.dailyCalories += 700;
                }
            }
        } catch (InterruptedException e) {
            Log.e("CalorieCalculation", "Error esperando el cálculo de calorías", e);
            // Usar un cálculo por defecto en caso de error
            this.dailyCalories = (int) Math.round(tmb * 1.5);
            if ("GAIN".equals(goalType)) {
                this.dailyCalories += 700;
            }
        }
    }
}
