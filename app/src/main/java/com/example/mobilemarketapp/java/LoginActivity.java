package com.example.mobilemarketapp.java;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilemarketapp.DBHelper;
import com.example.mobilemarketapp.MainActivity;
import com.example.mobilemarketapp.R;
import com.example.mobilemarketapp.RegisterActivity;

/**
 * LoginActivity — First screen shown when the app launches.
 *
 * If the user already has an active session stored in SharedPreferences
 * they are redirected straight to MainActivity without needing to log in again.
 *
 * Validates:
 *   - Email field not empty
 *   - Password field not empty
 *   - Credentials match a user record in the database
 *     (password is hashed inside DBHelper before the comparison)
 */
public class LoginActivity extends AppCompatActivity {

    // UI views
    EditText emailInput, passwordInput;
    Button loginBtn;
    TextView goToRegister;

    // Database helper
    com.example.mobilemarketapp.DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialise database helper
        dbHelper = new DBHelper(this);

        // Bind UI views
        emailInput    = findViewById(R.id.loginEmail);
        passwordInput = findViewById(R.id.loginPassword);
        loginBtn      = findViewById(R.id.loginBtn);
        goToRegister  = findViewById(R.id.goToRegister);

        // ── Auto-login: skip login screen if session already exists ──────────
        String savedUser = getSharedPreferences("app", MODE_PRIVATE)
            .getString("user", null);

        if (savedUser != null) {
            // Session found — go directly to the marketplace
            goToMain();
            return;
        }

        // ── Login button click handler ────────────────────────────────────────
        loginBtn.setOnClickListener(view -> {
            String email    = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Validate: email must not be empty
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate: password must not be empty
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check credentials against the database
            // DBHelper.loginUser() hashes the password internally before comparing
            if (dbHelper.loginUser(email, password)) {

                // Fetch the username to store in session
                Cursor cursor = dbHelper.getUserByEmail(email);
                String username = email; // fallback if cursor fails
                if (cursor != null && cursor.moveToFirst()) {
                    username = cursor.getString(1); // column 1 = username
                    cursor.close();
                }

                // Save session to SharedPreferences
                getSharedPreferences("app", MODE_PRIVATE)
                    .edit()
                    .putString("user",  username)
                    .putString("email", email)
                    .apply();

                goToMain();

            } else {
                // No matching record found → invalid credentials
                Toast.makeText(this, "Incorrect email or password!", Toast.LENGTH_SHORT).show();
            }
        });

        // Navigate to registration screen
        goToRegister.setOnClickListener(v ->
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    /** Navigates to MainActivity and removes LoginActivity from the back stack. */
    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
