package com.example.tfgdanielmario;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExerciseItemView extends LinearLayout {
    private TextView tvName;
    private ImageView ivDelete;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick();
    }

    public ExerciseItemView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.item_exercise, this, true);
        tvName = findViewById(R.id.tvNameExersice);
        ivDelete = findViewById(R.id.ivDelete);

        ivDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick();
            }
        });
    }

    public void setExercise(ExerciseRecord exercise) {
        String displayText = String.format("%s - %d series (%.1f kg)", 
            exercise.getExerciseName(), 
            exercise.getSets(), 
            exercise.getInitialWeight());
        tvName.setText(displayText);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }
} 