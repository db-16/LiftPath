package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etMail, etPassword;
    private Button btnLogin, btnRegisterRedirect;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        etMail = findViewById(R.id.etMail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegisterRedirect = findViewById(R.id.btnRegisterRedirect);

        btnLogin.setOnClickListener(view -> authenticateUser());

        btnRegisterRedirect.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void authenticateUser() {
        String mail = etMail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (mail.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_email_password), Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(mail, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MyWorkoutPlanActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, 
                            getString(R.string.login_failed, task.getException().getMessage()), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
