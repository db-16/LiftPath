package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditExerciseActivity extends AppCompatActivity {
    private static final String TAG = "EditExercise";

    private EditText etExerciseName, etSets, etInitialWeight;
    private Button btnSaveExercise;
    private ExerciseRecord exerciseToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exersice);

        // Configurar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Editar Ejercicio");
        }

        // Inicializar vistas
        etExerciseName = findViewById(R.id.etExerciseName);
        etSets = findViewById(R.id.etSets);
        etInitialWeight = findViewById(R.id.etInitialWeight);
        btnSaveExercise = findViewById(R.id.btnSave);

        // Obtener ejercicio a editar
        exerciseToEdit = (ExerciseRecord) getIntent().getSerializableExtra("exerciseRecord");

        if (exerciseToEdit == null) {
            Toast.makeText(this, "Error al cargar el ejercicio", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Precargar datos del ejercicio
        preloadExercise();

        // Configurar botón de guardar
        btnSaveExercise.setOnClickListener(v -> saveExercise());
    }

    private void preloadExercise() {
        etExerciseName.setText(exerciseToEdit.getExerciseName());
        etSets.setText(String.valueOf(exerciseToEdit.getSets()));
        etInitialWeight.setText(String.valueOf(exerciseToEdit.getInitialWeight()));
    }

    private void saveExercise() {
        String name = etExerciseName.getText().toString().trim();
        String setsStr = etSets.getText().toString().trim();
        String initialWeightStr = etInitialWeight.getText().toString().trim();

        if (name.isEmpty() || setsStr.isEmpty() || initialWeightStr.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int sets = Integer.parseInt(setsStr);
            float initialWeight = Float.parseFloat(initialWeightStr);

            // Actualizar el ejercicio existente
            exerciseToEdit.setExerciseName(name);
            exerciseToEdit.setSets(sets);
            exerciseToEdit.setInitialWeight(initialWeight);

            Log.d(TAG, "Actualizando ejercicio: " + name);
            
            Intent resultIntent = new Intent();
            resultIntent.putExtra("exercise", exerciseToEdit);
            setResult(RESULT_OK, resultIntent);
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa números válidos", Toast.LENGTH_SHORT).show();
        }
    }
} 