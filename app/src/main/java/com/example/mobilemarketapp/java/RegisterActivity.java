package com.example.mobilemarketapp.java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilemarketapp.DBHelper;
import com.example.mobilemarketapp.LoginActivity;
import com.example.mobilemarketapp.MainActivity;
import com.example.mobilemarketapp.R;

/**
 * RegisterActivity — New user sign-up screen.
 *
 * Validates:
 *   - Username not empty
 *   - Email format (using android.util.Patterns)
 *   - Password at least 6 characters
 *   - Password and confirm-password fields match
 *
 * On success: saves username + email to SharedPreferences (session)
 * and navigates to MainActivity.
 */
public class RegisterActivity extends AppCompatActivity {

    // UI fields
    EditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    TextView goToLogin;
    Button registerBtn;

    // Database helper
    com.example.mobilemarketapp.DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialise database helper
        dbHelper = new DBHelper(this);

        // Bind UI views to variables
        usernameInput        = findViewById(R.id.registerUsername);
        emailInput           = findViewById(R.id.registerEmail);
        passwordInput        = findViewById(R.id.registerPassword);
        confirmPasswordInput = findViewById(R.id.registerConfirmPassword); // added in XML
        registerBtn          = findViewById(R.id.registerBtn);
        goToLogin            = findViewById(R.id.goToLogin);

        // Navigate to login screen when "Already have an account?" is tapped
        goToLogin.setOnClickListener(v ->
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class))
        );

        // Handle register button click
        registerBtn.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String email    = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirm  = confirmPasswordInput.getText().toString().trim();

            // ── Validation checks ─────────────────────────────────────────────

            if (username.isEmpty()) {
                Toast.makeText(this, "Enter a username!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check email is in a valid format (e.g. user@domain.com)
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Enter a password!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Minimum password length
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Confirm password must match
            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ── Attempt database insert ───────────────────────────────────────
            // DBHelper.registerUser() hashes the password before storing it
            boolean success = dbHelper.registerUser(username, email, password);

            if (success) {
                // Save session so the user doesn't have to log in again immediately
                SharedPreferences.Editor editor =
                    getSharedPreferences("app", MODE_PRIVATE).edit();
                editor.putString("user",  username);
                editor.putString("email", email);
                editor.apply();

                Toast.makeText(this, "Account created! Welcome, " + username + "!", Toast.LENGTH_SHORT).show();

                // Go straight to the main marketplace screen
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish(); // remove RegisterActivity from the back stack
            } else {
                // UNIQUE constraint on email/username caused the insert to fail
                Toast.makeText(this, "Email or username already taken!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
