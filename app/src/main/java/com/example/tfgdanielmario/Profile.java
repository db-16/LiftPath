package com.example.tfgdanielmario;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tfgdanielmario.data.AppDatabase;
import com.example.tfgdanielmario.data.User;

public class Profile extends AppCompatActivity {

    private TextView tvCurrentWeight, tvGoal;
    private EditText etNewWeight;
    private Button btnSaveWeight;

    private int currentUserId = 1; // For demo, static user id. In real app, get logged in user id.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvGoal = findViewById(R.id.tvGoal);


        loadUserData();

        btnSaveWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNewWeight();
            }
        });
    }

    private void loadUserData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            User user = db.userDao().getUserById(currentUserId);
            if (user != null) {
                runOnUiThread(() -> {
                    tvCurrentWeight.setText("Current Weight: " + user.weight + " kg");
                    tvGoal.setText("Goal: " + user.goal);
                });
            }
        }).start();
    }

    private void saveNewWeight() {
        String newWeightStr = etNewWeight.getText().toString().trim();
        if (newWeightStr.isEmpty()) {
            Toast.makeText(this, "Please enter a weight", Toast.LENGTH_SHORT).show();
            return;
        }

        float newWeight;
        try {
            newWeight = Float.parseFloat(newWeightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid weight", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            User user = db.userDao().getUserById(currentUserId);
            if (user != null) {
                user.weight = newWeight;
                db.userDao().insertUser(user); // Using insertUser to update (Room replaces on conflict)
                runOnUiThread(() -> {
                    Toast.makeText(Profile.this, "Weight updated", Toast.LENGTH_SHORT).show();
                    tvCurrentWeight.setText("Current Weight: " + newWeight + " kg");
                    etNewWeight.setText("");
                });
            }
        }).start();
    }
}
