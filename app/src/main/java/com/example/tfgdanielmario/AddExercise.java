package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddExercise extends AppCompatActivity {
    private static final String TAG = "AddExercise";

    private EditText etExerciseName, etSets, etInitialWeight;
    private Button btnSaveExercise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exersice);

        // Habilitar el botón de retroceso en la barra de acción
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Añadir Ejercicio");
        }

        // Inicializar vistas
        etExerciseName = findViewById(R.id.etExerciseName);
        etSets = findViewById(R.id.etSets);
        etInitialWeight = findViewById(R.id.etInitialWeight);
        btnSaveExercise = findViewById(R.id.btnSave);

        // Configurar el botón de guardar
        btnSaveExercise.setOnClickListener(v -> saveExercise());
    }

    private void saveExercise() {
        // Validar campos
        String name = etExerciseName.getText().toString().trim();
        String setsStr = etSets.getText().toString().trim();
        String weightStr = etInitialWeight.getText().toString().trim();

        if (name.isEmpty() || setsStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int sets = Integer.parseInt(setsStr);
            float weight = Float.parseFloat(weightStr);

            // Crear nuevo ejercicio
            ExerciseRecord exercise = new ExerciseRecord(
                null, // sessionId se establecerá en AddTrainingSessionActivity
                name,
                0,   // reps por defecto
                sets,
                weight
            );

            // Devolver el resultado
            Intent resultIntent = new Intent();
            resultIntent.putExtra("exercise", exercise);
            setResult(RESULT_OK, resultIntent);
            
            Log.d(TAG, "Ejercicio creado: " + exercise.toString());
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa números válidos", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
