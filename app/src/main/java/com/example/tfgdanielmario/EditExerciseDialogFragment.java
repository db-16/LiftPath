package com.example.tfgdanielmario;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditExerciseDialogFragment extends DialogFragment {
    private ExerciseRecord exercise;
    private OnExerciseEditedListener listener;

    public interface OnExerciseEditedListener {
        void onExerciseEdited(ExerciseRecord exercise);
    }

    public static EditExerciseDialogFragment newInstance(ExerciseRecord exercise) {
        EditExerciseDialogFragment fragment = new EditExerciseDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("exercise", exercise);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);
        if (getArguments() != null) {
            exercise = getArguments().getParcelable("exercise");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_exercise, container, false);

        TextInputLayout tilName = view.findViewById(R.id.tilExerciseName);
        TextInputLayout tilSets = view.findViewById(R.id.tilSets);
        TextInputLayout tilWeight = view.findViewById(R.id.tilWeight);

        TextInputEditText etName = view.findViewById(R.id.etExerciseName);
        TextInputEditText etSets = view.findViewById(R.id.etSets);
        TextInputEditText etWeight = view.findViewById(R.id.etWeight);

        MaterialButton btnSave = view.findViewById(R.id.btnSave);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        // Establecer valores actuales
        if (exercise != null) {
            etName.setText(exercise.getExerciseName());
            etSets.setText(String.valueOf(exercise.getSets()));
            etWeight.setText(String.valueOf(exercise.getInitialWeight()));
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String setsStr = etSets.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();

            if (name.isEmpty()) {
                tilName.setError("El nombre es requerido");
                return;
            }

            if (setsStr.isEmpty()) {
                tilSets.setError("Las series son requeridas");
                return;
            }

            if (weightStr.isEmpty()) {
                tilWeight.setError("El peso es requerido");
                return;
            }

            try {
                int sets = Integer.parseInt(setsStr);
                float weight = Float.parseFloat(weightStr);

                exercise.setExerciseName(name);
                exercise.setSets(sets);
                exercise.setInitialWeight(weight);

                if (listener != null) {
                    listener.onExerciseEdited(exercise);
                }
                dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Por favor, ingresa números válidos", Toast.LENGTH_SHORT).show();
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

    public void setOnExerciseEditedListener(OnExerciseEditedListener listener) {
        this.listener = listener;
    }
} 