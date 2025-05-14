package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyEmailActivity extends AppCompatActivity {

    private TextView textViewVerify;
    private Button buttonCheckVerification, buttonResendEmail;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // UI Elements
        textViewVerify = findViewById(R.id.textViewVerify);
        buttonCheckVerification = findViewById(R.id.buttonCheckVerification);
        buttonResendEmail = findViewById(R.id.buttonResendEmail);

        if (user == null) {
            Toast.makeText(this, "No user signed in.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Send email verification if not already verified
        if (!user.isEmailVerified()) {
            sendVerificationEmail();
        }

        // Check verification status
        buttonCheckVerification.setOnClickListener(v -> {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    Toast.makeText(this, "Email Verified!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Email not verified. Check your inbox!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Resend verification email
        buttonResendEmail.setOnClickListener(v -> sendVerificationEmail());
    }

    private void sendVerificationEmail() {
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
