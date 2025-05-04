package com.example.tfgdanielmario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WorkoutPlanAdapter extends RecyclerView.Adapter<WorkoutPlanAdapter.ViewHolder> {

    private List<WorkoutDay> workoutDays;

    public WorkoutPlanAdapter(List<WorkoutDay> workoutDays) {
        this.workoutDays = workoutDays;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayTitle;
        ImageView ivArrow;

        public ViewHolder(View itemView) {
            super(itemView);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }

    @NonNull
    @Override
    public WorkoutPlanAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutPlanAdapter.ViewHolder holder, int position) {
        WorkoutDay day = workoutDays.get(position);
        holder.tvDayTitle.setText(day.getDayTitle());

    }

    @Override
    public int getItemCount() {
        return workoutDays.size();
    }
}
