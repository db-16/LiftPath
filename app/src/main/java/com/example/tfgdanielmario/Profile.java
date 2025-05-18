package com.example.tfgdanielmario;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class Profile extends AppCompatActivity {

    private TextView tvNameAge, tvJob, tvLocation, tvCurrentWeight, tvGoal;
    private EditText etNewWeight;
    private Button btnSaveWeight, btnSettings;

    private String currentUserMail; // Usaremos el correo del usuario como ID para obtener el documento de Firestore.

    private FirebaseFirestore db; // Instancia de FirebaseFirestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inicializamos Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Vinculamos los componentes de la UI
        tvNameAge = findViewById(R.id.tvNameAge);
        tvJob = findViewById(R.id.tvJob);
        tvLocation = findViewById(R.id.tvLocation);
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvGoal = findViewById(R.id.tvGoal);

        btnSettings = findViewById(R.id.btnSettings);

        // Suponemos que el correo del usuario actual se pasa al inicio de la actividad
        // Este correo debería ser recuperado de la sesión del usuario (lo vamos a recibir como un extra)
        currentUserMail = getIntent().getStringExtra("userMail"); // Cambiar "userMail" por el valor correcto

        // Cargar los datos del usuario
        loadUserData();

        // Guardar el nuevo peso
        btnSaveWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNewWeight();
            }
        });

        // Configuración del botón de configuración (puedes redirigir a otra actividad si lo necesitas)
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Aquí puedes redirigir a la actividad de configuración si es necesario
                Toast.makeText(Profile.this, "Settings clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        // Obtener los datos del usuario desde Firestore usando el correo (que es el ID del documento)
        db.collection("users").document(currentUserMail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Recuperamos los datos del usuario
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Actualizamos la interfaz con los datos del usuario
                            tvNameAge.setText(user.getName() );
                            tvCurrentWeight.setText("Current Weight: " + user.getWeight() + " kg");
                            tvGoal.setText("Goal: " + user.getGoalWeight() + " kg");
                        }
                    } else {
                        Toast.makeText(Profile.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Profile.this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveNewWeight() {
        String newWeightStr = etNewWeight.getText().toString().trim();
        if (newWeightStr.isEmpty()) {
            Toast.makeText(this, "Please enter a weight", Toast.LENGTH_SHORT).show();
            return;
        }

        float newWeight;
        try {
            newWeight = Float.parseFloat(newWeightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid weight", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar el peso en Firestore
        db.collection("users").document(currentUserMail)
                .update("weight", newWeight)  // Actualizamos solo el peso del usuario
                .addOnSuccessListener(aVoid -> {
                    // Mostrar mensaje de éxito y actualizar la vista
                    Toast.makeText(Profile.this, "Weight updated", Toast.LENGTH_SHORT).show();
                    tvCurrentWeight.setText("Current Weight: " + newWeight + " kg");
                    etNewWeight.setText("");  // Limpiar el campo de texto
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Profile.this, "Failed to update weight: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
