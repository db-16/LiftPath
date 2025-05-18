package com.example.tfgdanielmario;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WorkoutPlanAdapter extends RecyclerView.Adapter<WorkoutPlanAdapter.WorkoutViewHolder> {

    private List<TrainingSession> sessions;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TrainingSession session);
    }

    public WorkoutPlanAdapter(Context context, List<TrainingSession> sessions, OnItemClickListener listener) {
        this.context = context;
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_workout_day, parent, false);
        return new WorkoutViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        TrainingSession session = sessions.get(position);
        String sessionTitle = "Day " + (position + 1) + ": " + session.getName();
        holder.tvWorkoutDay.setText(sessionTitle);

        // Aquí asignamos el click listener al ítem completo
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(session);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkoutDay;
        ImageView ivArrow;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkoutDay = itemView.findViewById(R.id.tvWorkoutDay);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}
