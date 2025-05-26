package com.example.tfgdanielmario;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Date;

public class ExerciseProgress implements Parcelable {
    private Date timestamp;
    private int reps;
    private double weight;

    // Constructor vacío necesario para Firebase
    public ExerciseProgress() {}

    public ExerciseProgress(Date timestamp, int reps, double weight) {
        this.timestamp = timestamp;
        this.reps = reps;
        this.weight = weight;
    }

    protected ExerciseProgress(Parcel in) {
        long time = in.readLong();
        timestamp = time != 0 ? new Date(time) : null;
        reps = in.readInt();
        weight = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp != null ? timestamp.getTime() : 0);
        dest.writeInt(reps);
        dest.writeDouble(weight);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExerciseProgress> CREATOR = new Creator<ExerciseProgress>() {
        @Override
        public ExerciseProgress createFromParcel(Parcel in) {
            return new ExerciseProgress(in);
        }

        @Override
        public ExerciseProgress[] newArray(int size) {
            return new ExerciseProgress[size];
        }
    };

    // Getters y setters
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
} 