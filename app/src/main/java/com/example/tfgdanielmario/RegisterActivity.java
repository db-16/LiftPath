package com.example.tfgdanielmario;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etWeight, etMail, etGoalWeight, etHeight, etAge;
    private RadioGroup rgGender;
    private Button btnRegister;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etWeight = findViewById(R.id.etWeight);
        etMail = findViewById(R.id.etMail);
        etGoalWeight = findViewById(R.id.etGoalWeight);
        etHeight = findViewById(R.id.etHeight);
        etAge = findViewById(R.id.etAge);
        rgGender = findViewById(R.id.rgGender);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(view -> registerUser());
        
        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(view -> {
            finish(); // Volver a la actividad anterior (Login)
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String goalWeightStr = etGoalWeight.getText().toString().trim();
        String mail = etMail.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String gender = rgGender.getCheckedRadioButtonId() == R.id.rbMale ? "MALE" : "FEMALE";

        if (username.isEmpty() || password.isEmpty() || weightStr.isEmpty() || goalWeightStr.isEmpty() || 
            mail.isEmpty() || heightStr.isEmpty() || ageStr.isEmpty() || rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        double weight, goalWeight;
        int height, age;
        try {
            weight = Double.parseDouble(weightStr);
            goalWeight = Double.parseDouble(goalWeightStr);
            height = Integer.parseInt(heightStr);
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.invalid_number_format), Toast.LENGTH_SHORT).show();
            return;
        }

        // Determinar el tipo de objetivo basado en la comparación de pesos
        String goalType;
        if (goalWeight > weight) {
            goalType = "GAIN";
        } else if (goalWeight < weight) {
            goalType = "LOSE";
        } else {
            goalType = "MAINTAIN";
        }

        // Crear usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(mail, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Usuario creado con éxito, ahora guardamos datos extra en Firestore
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            User newUser = new User();
                            newUser.setName(username);
                            newUser.setWeight(weight);
                            newUser.setCurrentWeight(weight);
                            newUser.setGoalWeight(goalWeight);
                            newUser.setGoalType(goalType);
                            newUser.setMail(mail);
                            newUser.setIdRoutine("default");
                            newUser.setHeight(height);
                            newUser.setAge(age);
                            newUser.setGender(gender);
                            newUser.setId(userId);
                            
                            // Calcular calorías en un hilo separado
                            new Thread(() -> {
                                newUser.calculateDailyCalories();
                                
                                // Guardar usuario en Firestore en el hilo principal
                                runOnUiThread(() -> {
                                    db.collection("users")
                                            .document(userId)
                                            .set(newUser)
                                            .addOnSuccessListener(aVoid -> {
                                                // Crear las rutinas por defecto
                                                createDefaultRoutines(userId);
                                                Toast.makeText(RegisterActivity.this, 
                                                    getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, 
                                                getString(R.string.registration_error, e.getMessage()), Toast.LENGTH_SHORT).show());
                                });
                            }).start();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, 
                            getString(R.string.registration_error, task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createDefaultRoutines(String userId) {
        // Crear las tres rutinas por defecto
        createRoutine(userId, "Empuje", createPushExercises());
        createRoutine(userId, "Tirón", createPullExercises());
        createRoutine(userId, "Pierna", createLegExercises());
    }

    private void createRoutine(String userId, String name, List<ExerciseRecord> exercises) {
        TrainingSession session = new TrainingSession(userId, name);
        
        db.collection("trainingSessions")
                .add(session)
                .addOnSuccessListener(documentReference -> {
                    String sessionId = documentReference.getId();
                    WriteBatch batch = db.batch();
                    
                    for (ExerciseRecord exercise : exercises) {
                        exercise.setSessionId(sessionId);
                        exercise.setUserId(userId);
                        batch.set(
                            db.collection("trainingSessions")
                                .document(sessionId)
                                .collection("exercises")
                                .document(),
                            exercise
                        );
                    }
                    
                    batch.commit();
                });
    }

    private List<ExerciseRecord> createPushExercises() {
        List<ExerciseRecord> exercises = new ArrayList<>();
        exercises.add(new ExerciseRecord(null, "Press de Banca", 12, 4, 20.0f));
        exercises.add(new ExerciseRecord(null, "Press Militar", 12, 4, 15.0f));
        exercises.add(new ExerciseRecord(null, "Press Inclinado", 12, 3, 17.5f));
        exercises.add(new ExerciseRecord(null, "Extensiones de Tríceps", 15, 3, 10.0f));
        exercises.add(new ExerciseRecord(null, "Fondos en Paralelas", 12, 3, 0.0f));
        return exercises;
    }

    private List<ExerciseRecord> createPullExercises() {
        List<ExerciseRecord> exercises = new ArrayList<>();
        exercises.add(new ExerciseRecord(null, "Dominadas", 8, 4, 0.0f));
        exercises.add(new ExerciseRecord(null, "Remo con Barra", 12, 4, 30.0f));
        exercises.add(new ExerciseRecord(null, "Curl de Bíceps", 12, 3, 10.0f));
        exercises.add(new ExerciseRecord(null, "Remo en Polea", 12, 3, 35.0f));
        exercises.add(new ExerciseRecord(null, "Face Pull", 15, 3, 15.0f));
        return exercises;
    }

    private List<ExerciseRecord> createLegExercises() {
        List<ExerciseRecord> exercises = new ArrayList<>();
        exercises.add(new ExerciseRecord(null, "Sentadilla", 12, 4, 40.0f));
        exercises.add(new ExerciseRecord(null, "Peso Muerto", 10, 4, 50.0f));
        exercises.add(new ExerciseRecord(null, "Prensa de Piernas", 12, 3, 80.0f));
        exercises.add(new ExerciseRecord(null, "Extensiones de Cuádriceps", 15, 3, 30.0f));
        exercises.add(new ExerciseRecord(null, "Curl de Isquiotibiales", 15, 3, 25.0f));
        return exercises;
    }
}
