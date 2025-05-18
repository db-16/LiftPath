package com.example.tfgdanielmario;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etWeight, etMail, etGoalWeight;
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
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String goalWeightStr = etGoalWeight.getText().toString().trim();
        String mail = etMail.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || weightStr.isEmpty() || goalWeightStr.isEmpty() || mail.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight, goalWeight;
        try {
            weight = Double.parseDouble(weightStr);
            goalWeight = Double.parseDouble(goalWeightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Formato de peso inválido", Toast.LENGTH_SHORT).show();
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
                            newUser.setId(userId);
                            newUser.setName(username);
                            newUser.setWeight(weight);
                            newUser.setCurrentWeight(weight);
                            newUser.setGoalWeight(goalWeight);
                            newUser.setGoalType(goalType);
                            newUser.setMail(mail);
                            newUser.setIdRoutine("default");

                            db.collection("users")
                                    .document(userId)
                                    .set(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, 
                                        "Error en el registro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, 
                            "Error en el registro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
