package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editLocation, editEmail, editName, editPassword;
    private Button saveButton;
    private String locationUser, emailUser, nameUser, passwordUser, usernameUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize views
        editLocation = findViewById(R.id.editLocation);
        editEmail = findViewById(R.id.editEmail);
        editName = findViewById(R.id.editName);
        editPassword = findViewById(R.id.editPpassword);
        saveButton = findViewById(R.id.saveButton);

        // Load user data from Intent
        showData();

        // Set up save button click listener
        saveButton.setOnClickListener(view -> updateProfile());
    }

    private void showData() {
        Intent intent = getIntent();
        usernameUser = intent.getStringExtra("username");

        if (usernameUser == null || usernameUser.isEmpty()) {
            Toast.makeText(this, "Error: Username is missing", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity to prevent further errors
            return;
        }

        // Initialize reference to this specific user's node
        userRef = FirebaseDatabase.getInstance().getReference("users").child(usernameUser);

        System.out.println("Username: " + usernameUser);
        System.out.println("UserRef: " + userRef);

        // Set current values to EditText fields
        locationUser = intent.getStringExtra("location") != null ? intent.getStringExtra("location") : "";
        emailUser = intent.getStringExtra("email") != null ? intent.getStringExtra("email") : "";
        nameUser = intent.getStringExtra("name") != null ? intent.getStringExtra("name") : "";
        passwordUser = intent.getStringExtra("password") != null ? intent.getStringExtra("password") : "";

        editLocation.setText(locationUser);
        editEmail.setText(emailUser);
        editName.setText(nameUser);
        editPassword.setText(passwordUser);
    }

    // Java
    private void updateProfile() {
        boolean changesMade = false;

        // Trim the new input values for proper comparisons
        String newLocation = editLocation.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();
        String newName = editName.getText().toString().trim();
        String newPassword = editPassword.getText().toString().trim();

        if (!newLocation.equals(locationUser)) {
            userRef.child("location").setValue(newLocation).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update location", Toast.LENGTH_SHORT).show();
                }
            });
            locationUser = newLocation;
            changesMade = true;
        }

        if (!newEmail.equals(emailUser)) {
            userRef.child("email").setValue(newEmail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update email", Toast.LENGTH_SHORT).show();
                }
            });
            emailUser = newEmail;
            changesMade = true;
        }

        if (!newName.equals(nameUser)) {
            userRef.child("name").setValue(newName).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show();
                }
            });
            nameUser = newName;
            changesMade = true;
        }

        if (!newPassword.equals(passwordUser)) {
            userRef.child("password").setValue(newPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            });
            passwordUser = newPassword;
            changesMade = true;
        }

        if (changesMade) {
            Toast.makeText(this, "Profile update initiated", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show();
        }
    }
}