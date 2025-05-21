package com.example.signuploginrealtime;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private MessageAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private DatabaseReference databaseRef;
    private String currentUserId;
    private String otherUserId;
    private String otherUserName;
    private String chatId;

    public static void start(Context context, String otherUserId, String otherUserName) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("otherUserId", otherUserId);
        intent.putExtra("otherUserName", otherUserName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get intent extras
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");

        if (otherUserId == null || otherUserName == null) {
            Toast.makeText(this, "User information missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatId = generateChatId(currentUserId, otherUserId);
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Setup Toolbar
        setupToolbar();

        // Setup UI
        recyclerView = findViewById(R.id.recycler_view_messages);
        messageInput = findViewById(R.id.message_edit_text);
        ImageButton sendButton = findViewById(R.id.send_button);

        // Setup RecyclerView
        adapter = new MessageAdapter(messages, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load messages
        loadMessages();

        // Send message
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView userNameText = toolbar.findViewById(R.id.user_name);
        userNameText.setText(otherUserName);

        ImageButton backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private String generateChatId(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }

    private void loadMessages() {
        databaseRef.child("messages").child(chatId)
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Message message = data.getValue(Message.class);
                            if (message != null) {
                                messages.add(message);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        scrollToBottom();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        // Create message
        Message message = new Message(currentUserId, otherUserId, text, System.currentTimeMillis());

        // Save to Firebase
        databaseRef.child("messages").child(chatId).push().setValue(message)
                .addOnSuccessListener(aVoid -> {
                    messageInput.setText("");
                    updateChats(message);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateChats(Message message) {
        // Update both users' chat lists
        Map<String, Object> updates = new HashMap<>();

        // Current user's chat entry
        Map<String, Object> currentUserChat = new HashMap<>();
        currentUserChat.put("userId", otherUserId);
        currentUserChat.put("userName", otherUserName);
        currentUserChat.put("lastMessage", message.getMessageText());
        currentUserChat.put("timestamp", message.getTimestamp());
        currentUserChat.put("chatId", chatId);

        // Other user's chat entry
        Map<String, Object> otherUserChat = new HashMap<>();
        otherUserChat.put("userId", currentUserId);
        otherUserChat.put("userName", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        otherUserChat.put("lastMessage", message.getMessageText());
        otherUserChat.put("timestamp", message.getTimestamp());
        otherUserChat.put("chatId", chatId);

        updates.put("chats/" + currentUserId + "/" + otherUserId, currentUserChat);
        updates.put("chats/" + otherUserId + "/" + currentUserId, otherUserChat);

        databaseRef.updateChildren(updates);
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            recyclerView.smoothScrollToPosition(messages.size() - 1);
        }
    }
}
