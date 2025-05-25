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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsDialogFragment extends DialogFragment {

    public interface OnSettingsSavedListener {
        void onSettingsSaved();
    }

    private OnSettingsSavedListener listener;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public static SettingsDialogFragment newInstance() {
        return new SettingsDialogFragment();
    }

    public void setOnSettingsSavedListener(OnSettingsSavedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_settings, container, false);

        // Configurar el fondo transparente
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextInputEditText etCurrentWeight = view.findViewById(R.id.etCurrentWeight);
        TextInputEditText etGoalWeight = view.findViewById(R.id.etGoalWeight);
        TextInputEditText etHeight = view.findViewById(R.id.etHeight);
        TextInputEditText etAge = view.findViewById(R.id.etAge);
        MaterialButton btnSave = view.findViewById(R.id.btnSave);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        // Cargar datos actuales
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    etCurrentWeight.setText(String.format("%.1f", documentSnapshot.getDouble("currentWeight")));
                    etGoalWeight.setText(String.format("%.1f", documentSnapshot.getDouble("goalWeight")));
                    etHeight.setText(String.valueOf(documentSnapshot.getLong("height")));
                    etAge.setText(String.valueOf(documentSnapshot.getLong("age")));
                }
            });

        btnSave.setOnClickListener(v -> {
            String currentWeightStr = etCurrentWeight.getText().toString();
            String goalWeightStr = etGoalWeight.getText().toString();
            String heightStr = etHeight.getText().toString();
            String ageStr = etAge.getText().toString();

            if (currentWeightStr.isEmpty() || goalWeightStr.isEmpty() || 
                heightStr.isEmpty() || ageStr.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float currentWeight = Float.parseFloat(currentWeightStr);
                float goalWeight = Float.parseFloat(goalWeightStr);
                int height = Integer.parseInt(heightStr);
                int age = Integer.parseInt(ageStr);

                db.collection("users").document(userId)
                    .update(
                        "currentWeight", currentWeight,
                        "goalWeight", goalWeight,
                        "height", height,
                        "age", age
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), getString(R.string.settings_updated), Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onSettingsSaved();
                        }
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), 
                            String.format(getString(R.string.update_error), e.getMessage()), 
                            Toast.LENGTH_SHORT).show();
                    });

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), getString(R.string.invalid_number_format), Toast.LENGTH_SHORT).show();
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
} 