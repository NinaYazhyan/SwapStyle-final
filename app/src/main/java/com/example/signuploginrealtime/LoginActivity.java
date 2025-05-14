package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText loginUsername, loginPassword;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        Button loginButton = findViewById(R.id.login_button);
        TextView signupRedirectText = findViewById(R.id.signupRedirectText);

        loginButton.setOnClickListener(v -> attemptLogin());
        signupRedirectText.setOnClickListener(v -> navigateToSignup());
    }

    private void attemptLogin() {
        if (!validateInputs()) return;

        String username = loginUsername.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        authenticateUser(username, password);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (loginUsername.getText().toString().trim().isEmpty()) {
            loginUsername.setError("Username cannot be empty");
            isValid = false;
        }

        if (loginPassword.getText().toString().trim().isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            isValid = false;
        }

        return isValid;
    }

    private void authenticateUser(String username, String password) {
        Query userQuery = databaseReference.orderByChild("username").equalTo(username);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    loginUsername.setError("User does not exist");
                    loginUsername.requestFocus();
                    return;
                }

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && user.password.equals(password)) {
                        handleSuccessfulLogin(username, user.name, user.email);
                        return;
                    }
                }

                loginPassword.setError("Invalid password");
                loginPassword.requestFocus();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showDatabaseError(error);
            }
        });
    }

    private void handleSuccessfulLogin(String username, String name, String email) {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    private void navigateToSignup() {
        startActivity(new Intent(this, SignupActivity.class));
    }

    private void showDatabaseError(DatabaseError error) {
        Toast.makeText(this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    // Simple User model class
    private static class User {
        public String username;
        public String name;
        public String email;
        public String password;
    }
}