package com.example.tfgdanielmario;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tfgdanielmario.data.AppDatabase;
import com.example.tfgdanielmario.data.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etWeight;
    private Spinner spinnerGoal;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etWeight = findViewById(R.id.etWeight);
        spinnerGoal = findViewById(R.id.spinnerGoal);
        btnRegister = findViewById(R.id.btnRegister);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.goals_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(adapter);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String goal = spinnerGoal.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        float weight;
        try {
            weight = Float.parseFloat(weightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid weight", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(username, password, weight, goal);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.userDao().insertUser(user);
            runOnUiThread(() -> {
                Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
