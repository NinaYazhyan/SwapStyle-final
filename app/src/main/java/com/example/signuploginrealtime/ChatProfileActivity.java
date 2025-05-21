package com.example.signuploginrealtime;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatProfileActivity extends AppCompatActivity {
    private TextView userNameTextView;
    private TextView emailTextView;
    private DatabaseReference databaseReference;
    private String userId;

    public static void start(Context context, String userId, String userName) {
        Intent intent = new Intent(context, ChatProfileActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("userName", userName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_profile);

        userId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");

        if (userId == null) {
            Toast.makeText(this, "User information not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        userNameTextView = findViewById(R.id.user_name);
        userNameTextView.setText(userName);

        // Add email TextView if it exists in your layout
        emailTextView = findViewById(R.id.user_email);
        if (emailTextView == null) {
            // This means you haven't added the email TextView to your layout yet
            // You can add it later
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        loadUserProfile();
    }

    private void loadUserProfile() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Update username if available
                    String username = snapshot.child("username").getValue(String.class);
                    if (username != null && !username.isEmpty()) {
                        userNameTextView.setText(username);
                    }

                    // Update email if available and email TextView exists
                    if (emailTextView != null) {
                        String email = snapshot.child("email").getValue(String.class);
                        if (email != null && !email.isEmpty()) {
                            emailTextView.setText(email);
                        } else {
                            emailTextView.setText("Email not available");
                        }
                    }
                } else {
                    Toast.makeText(ChatProfileActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ChatProfileActivity.this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
