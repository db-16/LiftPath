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
            tvExerciseName.setText(exercise.getExerciseName());
            
            // Mostrar series completadas vs totales
            int completedSets = exercise.getProgress() != null ? exercise.getProgress().size() : 0;
            tvSetsReps.setText(String.format("%d/%d series x %d reps", 
                completedSets, 
                exercise.getSets(), 
                exercise.getReps()));
            
            // Mostrar peso actual y progreso
            if (exercise.getProgress() != null && !exercise.getProgress().isEmpty()) {
                // Encontrar el peso más alto
                double maxWeight = exercise.getProgress().get(0).getWeight();
                for (ExerciseProgress prog : exercise.getProgress()) {
                    if (prog.getWeight() > maxWeight) {
                        maxWeight = prog.getWeight();
                    }
                }
                tvCurrentWeight.setText(String.format("Último peso más alto: %.1f kg", maxWeight));
                
                // Si todas las series están completas, mostrar mensaje
                if (completedSets >= exercise.getSets()) {
                    tvCurrentWeight.append(" ✓ Ejercicio completado");
                }
            } else {
                tvCurrentWeight.setText(String.format("Último peso: %.1f kg", exercise.getInitialWeight()));
            }
            
            // Mostrar peso estimado si está disponible
            if (exercise.getEstimatedWeight() > 0 && completedSets < exercise.getSets()) {
                tvEstimatedWeight.setVisibility(View.VISIBLE);
                tvEstimatedWeight.setText(String.format("Peso sugerido: %.1f kg", exercise.getEstimatedWeight()));
            } else {
                tvEstimatedWeight.setVisibility(View.GONE);
            }

            // Configurar click listener en todo el item
            itemView.setOnClickListener(v -> listener.onExerciseClick(exercise));
        }
    }
}
