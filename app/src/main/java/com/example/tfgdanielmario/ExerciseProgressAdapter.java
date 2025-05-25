package com.example.tfgdanielmario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExerciseProgressAdapter extends RecyclerView.Adapter<ExerciseProgressAdapter.ViewHolder> {

    private final List<ExerciseRecord> exercises;
    private final OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(ExerciseRecord exercise);
    }

    public ExerciseProgressAdapter(List<ExerciseRecord> exercises, OnExerciseClickListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(exercises.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvExerciseName;
        private final TextView tvSetsReps;
        private final TextView tvCurrentWeight;
        private final TextView tvEstimatedWeight;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvSetsReps = itemView.findViewById(R.id.tvSetsReps);
            tvCurrentWeight = itemView.findViewById(R.id.tvCurrentWeight);
            tvEstimatedWeight = itemView.findViewById(R.id.tvEstimatedWeight);
        }

        public void bind(ExerciseRecord exercise, OnExerciseClickListener listener) {
            // Nombre del ejercicio
            tvExerciseName.setText(exercise.getExerciseName());
            
            // Series y repeticiones con progreso
            tvSetsReps.setText(itemView.getContext().getString(R.string.sets_reps_format,
                (exercise.getProgress() != null ? exercise.getProgress().size() : 0),
                exercise.getSets(),
                exercise.getReps()));
            
            // Peso actual/último peso usado
            if (exercise.getProgress() != null && !exercise.getProgress().isEmpty()) {
                double lastWeight = exercise.getProgress().get(exercise.getProgress().size() - 1).getWeight();
                tvCurrentWeight.setText(itemView.getContext().getString(R.string.last_weight, lastWeight));
                tvCurrentWeight.setVisibility(View.VISIBLE);
            } else {
                tvCurrentWeight.setText(itemView.getContext().getString(R.string.last_weight, exercise.getInitialWeight()));
                tvCurrentWeight.setVisibility(View.VISIBLE);
            }
            
            // Peso estimado
            if (exercise.getEstimatedWeight() > 0) {
                tvEstimatedWeight.setText(itemView.getContext().getString(R.string.suggested_weight, exercise.getEstimatedWeight()));
                tvEstimatedWeight.setVisibility(View.VISIBLE);
            } else {
                tvEstimatedWeight.setVisibility(View.GONE);
            }

            // Configurar click listener en todo el item
            itemView.setOnClickListener(v -> listener.onExerciseClick(exercise));
        }
    }
}
