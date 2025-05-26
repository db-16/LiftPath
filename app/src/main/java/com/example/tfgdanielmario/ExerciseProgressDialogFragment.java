package com.example.tfgdanielmario;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
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
        args.putParcelable(ARG_EXERCISE, exercise);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnProgressSavedListener(OnProgressSavedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            exercise = getArguments().getParcelable(ARG_EXERCISE);
        }

        View view = inflater.inflate(R.layout.dialog_save_progress, container, false);

        // Configurar el fondo transparente
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvName = view.findViewById(R.id.tvName);
        TextInputEditText etReps = view.findViewById(R.id.etRepetitions);
        TextInputEditText etLoad = view.findViewById(R.id.etLoad);
        MaterialButton btnSave = view.findViewById(R.id.btnSave);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        if (exercise != null) {
            tvName.setText(exercise.getExerciseName());
            
            // Pre-llenar con valores actuales o el último progreso
            if (exercise.getProgress() != null && !exercise.getProgress().isEmpty()) {
                ExerciseProgress ultimoProgreso = exercise.getProgress().get(exercise.getProgress().size() - 1);
                etReps.setText(String.valueOf(ultimoProgreso.getReps()));
                etLoad.setText(String.format("%.1f", ultimoProgreso.getWeight()));
            } else {
                etReps.setText(String.valueOf(exercise.getReps()));
                etLoad.setText(String.format("%.1f", exercise.getInitialWeight()));
            }

            // Verificar series completadas
            int seriesCompletadas = exercise.getProgress() != null ? exercise.getProgress().size() : 0;
            if (seriesCompletadas >= exercise.getSets()) {
                Toast.makeText(getContext(), 
                    getString(R.string.series_completed), 
                    Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }

        btnSave.setOnClickListener(v -> {
            String repsStr = etReps.getText().toString();
            String loadStr = etLoad.getText().toString();

            if (repsStr.isEmpty() || loadStr.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int reps = Integer.parseInt(repsStr);
                double load = Double.parseDouble(loadStr);

                if (reps <= 0 || load <= 0) {
                    Toast.makeText(getContext(), getString(R.string.values_greater_than_zero), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verificar si aún quedan series por completar
                int seriesCompletadas = exercise.getProgress() != null ? exercise.getProgress().size() : 0;
                if (seriesCompletadas >= exercise.getSets()) {
                    Toast.makeText(getContext(), 
                        getString(R.string.series_completed), 
                        Toast.LENGTH_SHORT).show();
                    dismiss();
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
                dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), getString(R.string.enter_valid_numbers), Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
