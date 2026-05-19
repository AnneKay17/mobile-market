package com.example.mobilemarketapp;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginBtn;
    TextView goToRegister, forgotPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput         = findViewById(R.id.loginEmail);
        passwordInput      = findViewById(R.id.loginPassword);
        loginBtn           = findViewById(R.id.loginBtn);
        goToRegister       = findViewById(R.id.goToRegister);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        String savedUser = getSharedPreferences("app", MODE_PRIVATE).getString("user", null);
        if (savedUser != null) {
            goToMain();
            return;
        }

        loginBtn.setOnClickListener(view -> {
            String email    = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isConnected()) {
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    JSONObject json = new JSONObject();
                    json.put("email", email);
                    json.put("password", password);

                    String response = ApiClient.post("login.php", json.toString());
                    JSONObject obj  = new JSONObject(response);
                    boolean success = obj.getBoolean("success");

                    runOnUiThread(() -> {
                        if (success) {
                            try {
                                String username = obj.getString("username");
                                getSharedPreferences("app", MODE_PRIVATE)
                                        .edit()
                                        .putString("user", username)
                                        .putString("email", email)
                                        .apply();
                                goToMain();
                            } catch (Exception e) {
                                Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Invalid email or password!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Connection error! Check your internet.", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });

        goToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        forgotPasswordText.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
