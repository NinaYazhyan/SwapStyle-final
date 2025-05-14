package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.storage.FirebaseStorage; // Added import

public class MainActivity extends AppCompatActivity {

    Button login_button_bt, signup_button_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Your existing button initialization
        login_button_bt = findViewById(R.id.login_button_bt);
        signup_button_bt = findViewById(R.id.signup_button_bt);

        // Added Firebase Storage initialization (single line)
        FirebaseStorage.getInstance(); // This initializes storage

        // Corrected click listener
        login_button_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        signup_button_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}