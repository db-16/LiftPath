package com.example.tfgdanielmario;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class ExerciseRecord implements Parcelable {
    private String id;
    private String sessionId;
    private String userId;
    private String exerciseName;
    private int reps;
    private int sets;
    private float initialWeight;
    private float estimatedWeight;
    private List<ExerciseProgress> progress;

    // Constructor vacío necesario para Firebase
    public ExerciseRecord() {
        progress = new ArrayList<>();
    }

    public ExerciseRecord(String sessionId, String exerciseName, int reps, int sets, float initialWeight) {
        this.sessionId = sessionId;
        this.exerciseName = exerciseName;
        this.reps = reps;
        this.sets = sets;
        this.initialWeight = initialWeight;
        this.estimatedWeight = initialWeight;
        this.progress = new ArrayList<>();
    }

    protected ExerciseRecord(Parcel in) {
        id = in.readString();
        sessionId = in.readString();
        userId = in.readString();
        exerciseName = in.readString();
        reps = in.readInt();
        sets = in.readInt();
        initialWeight = in.readFloat();
        estimatedWeight = in.readFloat();
        progress = new ArrayList<>();
        in.readList(progress, ExerciseProgress.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(sessionId);
        dest.writeString(userId);
        dest.writeString(exerciseName);
        dest.writeInt(reps);
        dest.writeInt(sets);
        dest.writeFloat(initialWeight);
        dest.writeFloat(estimatedWeight);
        dest.writeList(progress);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExerciseRecord> CREATOR = new Creator<ExerciseRecord>() {
        @Override
        public ExerciseRecord createFromParcel(Parcel in) {
            return new ExerciseRecord(in);
        }

        @Override
        public ExerciseRecord[] newArray(int size) {
            return new ExerciseRecord[size];
        }
    };

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public float getInitialWeight() {
        return initialWeight;
    }

    public void setInitialWeight(float initialWeight) {
        this.initialWeight = initialWeight;
    }

    public List<ExerciseProgress> getProgress() {
        return progress;
    }

    public void setProgress(List<ExerciseProgress> progress) {
        this.progress = progress;
    }

    public float getEstimatedWeight() {
        return estimatedWeight;
    }

    public void setEstimatedWeight(float estimatedWeight) {
        this.estimatedWeight = estimatedWeight;
    }

    public void addProgress(ExerciseProgress progress) {
        if (this.progress == null) {
            this.progress = new ArrayList<>();
        }
        this.progress.add(progress);
    }

    @Override
    public String toString() {
        return exerciseName + " - " + sets + " series (" + initialWeight + " kg)";
    }
}
