package com.example.signuploginrealtime;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private MessageAdapter adapter;
    private List<ChatMessage> messageList;

    private String receiverId;
    private String senderId;
    private String chatPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverId = getIntent().getStringExtra("receiverId");
        senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatPath = "chats/" + getChatId(senderId, receiverId);

        recyclerView = findViewById(R.id.recycler_chat);
        messageInput = findViewById(R.id.edit_message);
        sendButton = findViewById(R.id.btn_send);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendMessage());

        listenForMessages();
    }

    private String getChatId(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    private void sendMessage() {
        String msg = messageInput.getText().toString().trim();
        if (msg.isEmpty()) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(chatPath);
        ChatMessage chatMessage = new ChatMessage(senderId, receiverId, msg, System.currentTimeMillis());
        ref.push().setValue(chatMessage);

        messageInput.setText("");
    }

    private void listenForMessages() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(chatPath);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    ChatMessage msg = snap.getValue(ChatMessage.class);
                    messageList.add(msg);
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
