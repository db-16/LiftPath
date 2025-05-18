package com.example.tfgdanielmario;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Date;

public class ExerciseProgressDialogFragment extends DialogFragment {

    public interface OnProgressSavedListener {
        void onProgressSaved(ExerciseRecord updatedExercise);
    }

    private static final String ARG_EXERCISE = "exercise";
    private ExerciseRecord exercise;
    private OnProgressSavedListener listener;

    public static ExerciseProgressDialogFragment newInstance(ExerciseRecord exercise) {
        ExerciseProgressDialogFragment fragment = new ExerciseProgressDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EXERCISE, exercise);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnProgressSavedListener(OnProgressSavedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            exercise = (ExerciseRecord) getArguments().getSerializable(ARG_EXERCISE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_save_progress, null);

        TextView tvName = view.findViewById(R.id.tvName);
        EditText etReps = view.findViewById(R.id.etRepetitions);
        EditText etLoad = view.findViewById(R.id.etLoad);

        if (exercise != null) {
            tvName.setText(exercise.getExerciseName());
            
            // Mostrar el número de serie actual
            int seriesCompletadas = exercise.getProgress() != null ? exercise.getProgress().size() : 0;
            int seriesRestantes = exercise.getSets() - seriesCompletadas;
            String titulo = String.format("Guardar progreso (Serie %d/%d)", 
                seriesCompletadas + 1, exercise.getSets());
            
            // Pre-llenar con valores actuales o el último progreso
            if (exercise.getProgress() != null && !exercise.getProgress().isEmpty()) {
                ExerciseProgress ultimoProgreso = exercise.getProgress().get(exercise.getProgress().size() - 1);
                etReps.setText(String.valueOf(ultimoProgreso.getReps()));
                etLoad.setText(String.format("%.1f", ultimoProgreso.getWeight()));
            } else {
                etReps.setText(String.valueOf(exercise.getReps()));
                etLoad.setText(String.format("%.1f", exercise.getInitialWeight()));
            }

            builder.setTitle(titulo);
            if (seriesRestantes <= 0) {
                Toast.makeText(getContext(), 
                    "Ya has completado todas las series de este ejercicio", 
                    Toast.LENGTH_SHORT).show();
                return builder.create();
            }
        }

        builder.setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String repsStr = etReps.getText().toString();
                    String loadStr = etLoad.getText().toString();

                    if (repsStr.isEmpty() || loadStr.isEmpty()) {
                        Toast.makeText(getContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        int reps = Integer.parseInt(repsStr);
                        double load = Double.parseDouble(loadStr);

                        if (reps <= 0 || load <= 0) {
                            Toast.makeText(getContext(), "Los valores deben ser mayores que 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Verificar si aún quedan series por completar
                        int seriesCompletadas = exercise.getProgress() != null ? exercise.getProgress().size() : 0;
                        if (seriesCompletadas >= exercise.getSets()) {
                            Toast.makeText(getContext(), 
                                "Ya has completado todas las series de este ejercicio", 
                                Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Crear nuevo registro de progreso
                        ExerciseProgress progress = new ExerciseProgress(
                            new Date(),
                            reps,
                            load
                        );

                        // Actualizar el ejercicio
                        if (exercise.getProgress() == null) {
                            exercise.setProgress(new ArrayList<>());
                        }
                        exercise.getProgress().add(progress);

                        // Actualizar los valores actuales del ejercicio
                        exercise.setReps(reps);
                        exercise.setInitialWeight((float)load);

                        if (listener != null) {
                            listener.onProgressSaved(exercise);
                        }

                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Por favor ingresa valores numéricos válidos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null);

        return builder.create();
    }
}
