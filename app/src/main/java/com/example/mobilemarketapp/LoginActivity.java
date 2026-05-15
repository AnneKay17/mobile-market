package com.example.mobilemarketapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText useremail, password;
    TextView goToRegister;
    Button loginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //connect java to login layout
        setContentView(R.layout.activity_login);

        useremail = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        goToRegister = findViewById(R.id.goToRegister);

        loginBtn.setOnClickListener(view -> {
            String email = useremail.getText().toString();
            String pass = password.getText().toString();

            if(UserStore.login(email, pass)){

                //save logged in user
                getSharedPreferences("app", MODE_PRIVATE)
                        .edit()
                        .putString("user", email)
                        .apply();

                //go to main screen
                startActivity(new Intent(this, MainActivity.class));
            }
            else{
                Toast.makeText(this, "Invalid Login", Toast.LENGTH_SHORT).show();
            }
        });

        //redirects user to register
        goToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        //Auto-login check
        String savedUser = getSharedPreferences("app", MODE_PRIVATE)
                .getString("user", null);
        if(savedUser != null){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }



}
