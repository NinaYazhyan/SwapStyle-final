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
        editPassword = findViewById(R.id.editPassword);
        saveButton = findViewById(R.id.saveButton);

        // Load user data from Intent
        showData();

        // Set up save button click listener
        saveButton.setOnClickListener(view -> updateProfile());
    }

    private void showData() {
        Intent intent = getIntent();
        usernameUser = intent.getStringExtra("username");
        String userId = intent.getStringExtra("userId");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error: User ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Используем userId вместо username для ссылки на данные пользователя
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Получаем данные из Intent
        locationUser = intent.getStringExtra("location") != null ? intent.getStringExtra("location") : "";
        emailUser = intent.getStringExtra("email") != null ? intent.getStringExtra("email") : "";
        nameUser = intent.getStringExtra("name") != null ? intent.getStringExtra("name") : "";
        passwordUser = intent.getStringExtra("password") != null ? intent.getStringExtra("password") : "";

        // Заполняем поля
        editLocation.setText(locationUser);
        editEmail.setText(emailUser);
        editName.setText(nameUser);
        editPassword.setText(passwordUser);
    }

    private void updateProfile() {
        boolean changesMade = false;

        // Получаем новые значения из полей ввода
        String newLocation = editLocation.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();
        String newName = editName.getText().toString().trim();
        String newPassword = editPassword.getText().toString().trim();

        // Обновляем местоположение, если оно изменилось
        if (!newLocation.equals(locationUser)) {
            userRef.child("location").setValue(newLocation);
            locationUser = newLocation;
            changesMade = true;
        }

        // Обновляем email, если он изменился
        if (!newEmail.equals(emailUser)) {
            userRef.child("email").setValue(newEmail);
            emailUser = newEmail;
            changesMade = true;
        }

        // Обновляем имя, если оно изменилось
        if (!newName.equals(nameUser)) {
            userRef.child("name").setValue(newName);
            nameUser = newName;
            changesMade = true;
        }

        // Обновляем пароль, если он изменился
        if (!newPassword.equals(passwordUser)) {
            userRef.child("password").setValue(newPassword);
            passwordUser = newPassword;
            changesMade = true;
        }

        // Уведомляем пользователя о результате
        if (changesMade) {
            Toast.makeText(this, "Profile successfully updated", Toast.LENGTH_SHORT).show();
            // Обновляем имя пользователя, если оно изменилось
            if (!newName.equals(nameUser)) {
                userRef.child("username").setValue(usernameUser);
            }
            finish();
        } else {
            Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show();
        }
    }
}