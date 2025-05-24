package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity {

    Button login_button_bt, signup_button_bt, test_user_button;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Button initialization
        login_button_bt = findViewById(R.id.login_button_bt);
        signup_button_bt = findViewById(R.id.signup_button_bt);
        test_user_button = findViewById(R.id.test_user_button);

        FirebaseStorage.getInstance();

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

        // Test User login button
        test_user_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAsTestUser();
            }
        });
    }

    private void loginAsTestUser() {
        test_user_button.setEnabled(false);
        test_user_button.setText("Logging in...");

        // Find the test user by username in database
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("username").equalTo("test user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        if (userSnapshot.child("email").exists()) {
                            String email = userSnapshot.child("email").getValue(String.class);
                            // Now login with the found email and known password
                            loginWithCredentials(email, "Samsung2025");
                            return;
                        }
                    }
                    loginFailed("Test user found but email missing");
                } else {
                    loginFailed("Test user not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loginFailed("Database error: " + error.getMessage());
            }
        });
    }

    private void loginWithCredentials(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // First find the complete user data to pass to DashboardActivity
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        String username = userSnapshot.child("username").getValue(String.class);
                                        String name = userSnapshot.child("name").getValue(String.class);

                                        // Create intent with user data
                                        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                        intent.putExtra("username", username);
                                        intent.putExtra("name", name);
                                        intent.putExtra("email", email);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                        return;
                                    }
                                }
                                // If we couldn't find the user data, just start DashboardActivity without extras
                                startDashboardActivity();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                startDashboardActivity();
                            }
                        });
                    } else {
                        loginFailed("Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    private void startDashboardActivity() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loginFailed(String message) {
        test_user_button.setEnabled(true);
        test_user_button.setText("Test User");
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}