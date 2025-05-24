package com.example.signuploginrealtime;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";


    EditText signupName, signupEmail, signupUsername, signupPassword, signupLocation;
    TextView loginRedirectText;
    Button signupButton;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        // Initialize views
        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        signupLocation = findViewById(R.id.signup_location);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);


        signupButton.setOnClickListener(view -> {
            // Get input values
            String name = signupName.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String username = signupUsername.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();
            String location = signupLocation.getText().toString().trim();


            // Validate inputs
            if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }


            // Create user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Send verification email
                                firebaseUser.sendEmailVerification()
                                        .addOnCompleteListener(verifyTask -> {
                                            if (verifyTask.isSuccessful()) {
                                                Toast.makeText(SignupActivity.this,
                                                        "Verification email sent. Please check your email.",
                                                        Toast.LENGTH_LONG).show();


                                                // Save user to database
                                                saveUserToDatabase(firebaseUser.getUid(), name, email, username, password, location);


                                                // Redirect to VerifyEmailActivity
                                                Intent intent = new Intent(SignupActivity.this, VerifyEmailActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Log.e(TAG, "Failed to send verification email", verifyTask.getException());
                                                Toast.makeText(SignupActivity.this,
                                                        "Failed to send verification email: " + verifyTask.getException().getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Log.e(TAG, "Signup failed", task.getException());
                            Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        loginRedirectText.setOnClickListener(view -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }


    private void saveUserToDatabase(String userId, String name, String email, String username, String password, String location) {
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");


        // Create user object
        HelperClass helperClass = new HelperClass(name, email, username, password, location);


        // Save to Firebase
        reference.child(userId).setValue(helperClass)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Failed to save user data", task.getException());
                        Toast.makeText(SignupActivity.this,
                                "Failed to save user data: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

