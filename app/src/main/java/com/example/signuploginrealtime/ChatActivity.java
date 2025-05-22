package com.example.signuploginrealtime;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String userId;
    private String userName;
    private String currentUserId;
    private String currentUserName;

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messageList;
    private EditText messageEditText;
    private DatabaseReference messagesRef;
    private DatabaseReference recentChatsRef;

    public static void start(Context context, String userId, String userName) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("userName", userName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get data from intent
        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");

        // Get current user info
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        getCurrentUserName();

        // Initialize Firebase references
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        messagesRef = databaseRef.child("messages");
        recentChatsRef = databaseRef.child("recent_chats");

        // Set up UI
        TextView userNameTextView = findViewById(R.id.user_name);
        userNameTextView.setText(userName);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recycler_view_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList, currentUserId);
        recyclerView.setAdapter(adapter);

        // Set up message input
        messageEditText = findViewById(R.id.message_edit_text);
        ImageButton sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(v -> sendMessage());

        // Load messages
        loadMessages();
    }

    private void getCurrentUserName() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("username").exists()) {
                    currentUserName = snapshot.child("username").getValue(String.class);
                } else {
                    currentUserName = "Unknown User";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                currentUserName = "Unknown User";
            }
        });
    }

    private void loadMessages() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();

                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null &&
                            ((message.getSenderId().equals(currentUserId) && message.getReceiverId().equals(userId)) ||
                                    (message.getSenderId().equals(userId) && message.getReceiverId().equals(currentUserId)))) {
                        messageList.add(message);
                    }
                }

                // Sort messages by timestamp
                Collections.sort(messageList, (m1, m2) ->
                        Long.compare(m1.getTimestamp(), m2.getTimestamp()));

                adapter.notifyDataSetChanged();

                // Scroll to bottom
                if (messageList.size() > 0) {
                    recyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        long timestamp = System.currentTimeMillis();
        Message message = new Message(currentUserId, userId, messageText, timestamp);

        // Add message to Firebase
        String messageId = messagesRef.push().getKey();
        if (messageId != null) {
            messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        messageEditText.setText("");
                        updateRecentChats(messageText, timestamp);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateRecentChats(String messageText, long timestamp) {
        // Update for current user
        RecentChat myRecentChat = new RecentChat(userId, userName, messageText, timestamp);
        recentChatsRef.child(currentUserId).child(userId).setValue(myRecentChat);

        // Update for other user
        RecentChat theirRecentChat = new RecentChat(currentUserId, currentUserName, messageText, timestamp);
        recentChatsRef.child(userId).child(currentUserId).setValue(theirRecentChat);
    }
}