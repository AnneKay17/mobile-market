package com.example.mobilemarketapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    EditText username, useremail, password;
    TextView goToLogin;
    Button createAcc;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //connect to layout
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.registerUsername);
        useremail = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        createAcc = findViewById(R.id.registerBtn);
        goToLogin = findViewById(R.id.goToLogin);


        goToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });

        createAcc.setOnClickListener(view -> {
            String user = username.getText().toString();
            String email = useremail.getText().toString();
            String pass = password.getText().toString();

            if(UserStore.register(user, email, pass)){

                // Save user session
                SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString("user", user);   // username for profile
                editor.putString("email", email);
                editor.apply();

                Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                // go to main app (better UX than finish)
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }
            else{
                Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
