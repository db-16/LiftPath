package com.example.tfgdanielmario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {

    private final List<ExerciseRecord> exercises;
    private final OnItemActionListener listener;

    public interface OnItemActionListener {
        void onEdit(ExerciseRecord exercise);
        void onDelete(ExerciseRecord exercise);
    }

    public ExerciseAdapter(List<ExerciseRecord> exercises, OnItemActionListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseRecord exercise = exercises.get(position);
        holder.bind(exercise, listener);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final ImageView btnEdit;
        private final ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvNameExersice);
            btnEdit = itemView.findViewById(R.id.ivEdit);
            btnDelete = itemView.findViewById(R.id.ivDelete);
        }

        public void bind(ExerciseRecord exercise, OnItemActionListener listener) {
            String displayText = String.format("%s - %d series (%.1f kg)", 
                exercise.getExerciseName(), 
                exercise.getSets(), 
                exercise.getInitialWeight());
            tvName.setText(displayText);

            btnEdit.setOnClickListener(v -> listener.onEdit(exercise));
            btnDelete.setOnClickListener(v -> listener.onDelete(exercise));
        }
    }
}
