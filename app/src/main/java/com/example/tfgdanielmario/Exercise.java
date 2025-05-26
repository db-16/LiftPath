package com.example.tfgdanielmario;

import android.os.Parcel;
import android.os.Parcelable;

public class Exercise implements Parcelable {
    private String id;
    private String name;
    private int sets;
    private int reps;
    private double weight;

    public Exercise() {
        // Constructor vacío requerido para Firestore
    }

    public Exercise(String name, int sets, int reps, double weight) {
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
    }

    protected Exercise(Parcel in) {
        id = in.readString();
        name = in.readString();
        sets = in.readInt();
        reps = in.readInt();
        weight = in.readDouble();
    }

    public static final Creator<Exercise> CREATOR = new Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(sets);
        dest.writeInt(reps);
        dest.writeDouble(weight);
    }
}
