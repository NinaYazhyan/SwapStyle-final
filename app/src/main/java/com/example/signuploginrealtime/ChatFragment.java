package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";

    // UI Components
    private RecyclerView recyclerView;
    private TextView emptyStateTextView;
    private EditText searchEditText;
    private ImageButton searchButton;
    private FloatingActionButton fabNewChat;
    private View loadingView;

    // Adapter and Data
    private RecentChatAdapter recentChatAdapter;
    private List<RecentChat> recentChatList;
    private List<RecentChat> allChatsList;

    // Firebase
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Initialize Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return view;
        }

        currentUserId = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        initViews(view);
        setupRecyclerView();
        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUser != null) {
            loadRecentChats();
            if (recentChatAdapter != null) {
                recentChatAdapter.updateCurrentDay();
            }
        }
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_chats);
        emptyStateTextView = view.findViewById(R.id.empty_state_text);
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);
        fabNewChat = view.findViewById(R.id.fab_new_chat);

        // Loading view might be null if not in layout
        loadingView = view.findViewById(R.id.loading_view);
    }

    private void setupRecyclerView() {
        recentChatList = new ArrayList<>();
        allChatsList = new ArrayList<>();

        recentChatAdapter = new RecentChatAdapter(recentChatList, chat -> {
            if (chat != null && !TextUtils.isEmpty(chat.getUserId())) {
                ChatActivity.start(requireContext(), chat.getUserId(), chat.getUserName());
            } else {
                Toast.makeText(getContext(), "Invalid chat information", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recentChatAdapter);
    }

    private void setupListeners() {
        searchButton.setOnClickListener(v -> filterChats());

        fabNewChat.setOnClickListener(v -> {
            Log.d(TAG, "FAB clicked - attempting to start UserSearchActivity");
            Toast.makeText(getContext(), "FAB Clicked", Toast.LENGTH_SHORT).show();

            try {
                // Check if we're properly attached to an activity
                if (getActivity() == null) {
                    Log.e(TAG, "Cannot start UserSearchActivity - fragment not attached to activity");
                    return;
                }

                Intent intent = new Intent(getActivity(), UserSearchActivity.class);
                Log.d(TAG, "Starting UserSearchActivity...");
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting UserSearchActivity: " + e.getMessage(), e);
                Toast.makeText(getContext(), "Error starting user search: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void loadRecentChats() {
        showLoading();

        // Reference to the current user's chats
        DatabaseReference userChatsRef = databaseReference.child("chats").child(currentUserId);

        userChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<RecentChat> chatList = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        try {
                            String userId = chatSnapshot.getKey();
                            String userName = chatSnapshot.child("userName").getValue(String.class);
                            String lastMessage = chatSnapshot.child("lastMessage").getValue(String.class);
                            Long timestamp = chatSnapshot.child("timestamp").getValue(Long.class);

                            if (userId != null && userName != null && lastMessage != null && timestamp != null) {
                                RecentChat chat = new RecentChat(userId, userName, lastMessage, timestamp);
                                chatList.add(chat);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing chat data: " + e.getMessage());
                        }
                    }

                    // Sort by timestamp (newest first)
                    Collections.sort(chatList, (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
                }

                // Update data lists
                allChatsList.clear();
                allChatsList.addAll(chatList);

                // Apply current filter if search box has text
                String currentSearch = searchEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(currentSearch)) {
                    filterChats();
                } else {
                    // Show all chats
                    recentChatList.clear();
                    recentChatList.addAll(chatList);
                    recentChatAdapter.notifyDataSetChanged();

                    updateEmptyState();
                }

                hideLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load chats: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load chats", Toast.LENGTH_SHORT).show();
                hideLoading();
                updateEmptyState();
            }
        });
    }

    private void filterChats() {
        String query = searchEditText.getText().toString().trim().toLowerCase();

        if (TextUtils.isEmpty(query)) {
            // Show all chats if search is empty
            recentChatList.clear();
            recentChatList.addAll(allChatsList);
        } else {
            // Filter by username
            List<RecentChat> filteredList = new ArrayList<>();
            for (RecentChat chat : allChatsList) {
                if (chat.getUserName().toLowerCase().contains(query)) {
                    filteredList.add(chat);
                }
            }

            recentChatList.clear();
            recentChatList.addAll(filteredList);
        }

        recentChatAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (recentChatList.isEmpty()) {
            String query = searchEditText.getText().toString().trim();
            String message = TextUtils.isEmpty(query) ? "No chats yet" : "No matching chats found";
            showEmptyState(message);
        } else {
            hideEmptyState();
        }
    }

    private void showEmptyState(String message) {
        if (emptyStateTextView != null) {
            emptyStateTextView.setText(message);
            emptyStateTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (emptyStateTextView != null) {
            emptyStateTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading() {
        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
    }

    private void navigateToLogin() {
        // If you have a login activity, redirect there
        try {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to navigate to login: " + e.getMessage());
        }
    }
}
