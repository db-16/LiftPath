package com.example.tfgdanielmario;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddExerciseDialogFragment extends DialogFragment {

    private OnExerciseAddedListener listener;

    public interface OnExerciseAddedListener {
        void onExerciseAdded(ExerciseRecord exercise);
    }

    public void setOnExerciseAddedListener(OnExerciseAddedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_exercise, container, false);

        // Configurar el fondo transparente
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextInputEditText etName = view.findViewById(R.id.etExerciseName);
        TextInputEditText etSets = view.findViewById(R.id.etSets);
        TextInputEditText etReps = view.findViewById(R.id.etReps);
        TextInputEditText etWeight = view.findViewById(R.id.etWeight);
        MaterialButton btnSave = view.findViewById(R.id.btnSave);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String setsStr = etSets.getText().toString().trim();
            String repsStr = etReps.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();

            if (name.isEmpty() || setsStr.isEmpty() || repsStr.isEmpty() || weightStr.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int sets = Integer.parseInt(setsStr);
                int reps = Integer.parseInt(repsStr);
                float weight = Float.parseFloat(weightStr);

                ExerciseRecord exercise = new ExerciseRecord();
                exercise.setExerciseName(name);
                exercise.setSets(sets);
                exercise.setReps(reps);
                exercise.setInitialWeight(weight);

                if (listener != null) {
                    listener.onExerciseAdded(exercise);
                }
                dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), getString(R.string.invalid_number_format), Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }
} 