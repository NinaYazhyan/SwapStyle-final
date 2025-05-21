package com.example.signuploginrealtime;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserSearchActivity extends AppCompatActivity {
    private static final String TAG = "UserSearchActivity";

    private EditText searchEditText;
    private RecyclerView recyclerView;
    private TextView emptyStateTextView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference databaseReference;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        currentUserId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        searchEditText = findViewById(R.id.search_edit_text);
        recyclerView = findViewById(R.id.recycler_view_users);
        emptyStateTextView = findViewById(R.id.empty_state_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this::startChat);
        recyclerView.setAdapter(userAdapter);

        ImageButton searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> searchUsers());

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 2) searchUsers();
                else if (s.length() == 0) searchUsers();
            }
        });

        searchUsers();
    }

    private void searchUsers() {
        String query = searchEditText.getText().toString().trim().toLowerCase();
        Query searchQuery;

        if (TextUtils.isEmpty(query)) {
            searchQuery = databaseReference.child("users").limitToFirst(50);
        } else {
            searchQuery = databaseReference.child("users")
                    .orderByChild("username")
                    .startAt(query)
                    .endAt(query + "\uf8ff")
                    .limitToFirst(50);
        }

        searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> results = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null && !userId.equals(currentUserId)) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            user.setUserId(userId);
                            results.add(user);
                        }
                    }
                }
                updateUI(results);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Search failed: " + error.getMessage());
                updateUI(new ArrayList<>());
            }
        });
    }

    private void updateUI(List<User> results) {
        userList.clear();
        userList.addAll(results);
        userAdapter.notifyDataSetChanged();

        if (results.isEmpty()) {
            showEmptyState("No users found");
        } else {
            hideEmptyState();
        }
    }

    private void showEmptyState(String message) {
        emptyStateTextView.setText(message);
        emptyStateTextView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyStateTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void startChat(User user) {
        if (user == null || user.getUserId() == null) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            return;
        }
        ChatActivity.start(this, user.getUserId(), user.getUsername());
        finish();
    }
}
