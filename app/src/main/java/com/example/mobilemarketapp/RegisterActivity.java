package com.example.mobilemarketapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    EditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    TextView goToLogin;
    Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput        = findViewById(R.id.registerUsername);
        emailInput           = findViewById(R.id.registerEmail);
        passwordInput        = findViewById(R.id.registerPassword);
        confirmPasswordInput = findViewById(R.id.registerConfirmPassword);
        registerBtn          = findViewById(R.id.registerBtn);
        goToLogin            = findViewById(R.id.goToLogin);

        goToLogin.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class))
        );

        registerBtn.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String email    = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirm  = confirmPasswordInput.getText().toString().trim();

            if (username.isEmpty()) {
                Toast.makeText(this, "Enter a username!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Enter a password!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    JSONObject json = new JSONObject();
                    json.put("username", username);
                    json.put("email", email);
                    json.put("password", password);

                    String response = ApiClient.post("register.php", json.toString());
                    JSONObject obj  = new JSONObject(response);
                    boolean success = obj.getBoolean("success");

                    runOnUiThread(() -> {
                        if (success) {
                            SharedPreferences.Editor editor = getSharedPreferences("app", MODE_PRIVATE).edit();
                            editor.putString("user", username);
                            editor.putString("email", email);
                            editor.apply();

                            Toast.makeText(this, "Account created! Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Email or username already taken!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Connection error!", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });
    }
}
