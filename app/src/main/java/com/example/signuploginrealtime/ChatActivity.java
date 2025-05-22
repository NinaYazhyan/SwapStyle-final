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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final String EXTRA_USER_ID = "userId";
    private static final String EXTRA_USER_NAME = "userName";

    private RecyclerView recyclerViewMessages;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ImageButton backButton;
    private TextView usernameTextView;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private String currentUserId;
    private String chatUserId;
    private String chatUserName;
    private String currentUserName;

    private DatabaseReference messagesRef;
    private DatabaseReference recentChatsRef;

    public static void start(Context context, String userId, String userName) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_USER_NAME, userName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Проверка авторизации
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Необходимо войти в систему", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        currentUserId = currentUser.getUid();

        // Получение данных собеседника
        chatUserId = getIntent().getStringExtra(EXTRA_USER_ID);
        chatUserName = getIntent().getStringExtra(EXTRA_USER_NAME);

        if (chatUserId == null || chatUserName == null) {
            Toast.makeText(this, "Ошибка: данные пользователя отсутствуют", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализация Firebase
        String chatId = getChatId(currentUserId, chatUserId);
        messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);
        recentChatsRef = FirebaseDatabase.getInstance().getReference("recent_chats");

        // Инициализация UI
        initUI();

        // Получение имени текущего пользователя
        getCurrentUserName();

        // Загрузка сообщений
        loadMessages();
    }

    private void initUI() {
        recyclerViewMessages = findViewById(R.id.recycler_view_messages);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        backButton = findViewById(R.id.back_button);
        usernameTextView = findViewById(R.id.user_name);

        usernameTextView.setText(chatUserName);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);

        sendButton.setOnClickListener(v -> sendMessage());
        backButton.setOnClickListener(v -> finish());
    }

    private void getCurrentUserName() {
        FirebaseDatabase.getInstance().getReference("users").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("username")) {
                            currentUserName = snapshot.child("username").getValue(String.class);
                        } else {
                            currentUserName = "Пользователь";
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        currentUserName = "Пользователь";
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
                    if (message != null) {
                        message.setMessageId(messageSnapshot.getKey());
                        messageList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                if (messageList.size() > 0) {
                    recyclerViewMessages.smoothScrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Ошибка загрузки сообщений", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String content = messageEditText.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        long timestamp = System.currentTimeMillis();
        Message message = new Message(currentUserId, content, timestamp);

        // Сохранение сообщения
        String messageId = messagesRef.push().getKey();
        if (messageId != null) {
            messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        messageEditText.setText("");
                        updateRecentChats(content, timestamp);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ChatActivity.this, "Ошибка отправки: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void updateRecentChats(String lastMessage, long timestamp) {
        // Обновление для текущего пользователя
        RecentChat myRecentChat = new RecentChat(chatUserId, chatUserName, lastMessage, timestamp);
        recentChatsRef.child(currentUserId).child(chatUserId).setValue(myRecentChat);

        // Обновление для собеседника
        RecentChat theirRecentChat = new RecentChat(currentUserId, currentUserName, lastMessage, timestamp);
        recentChatsRef.child(chatUserId).child(currentUserId).setValue(theirRecentChat);
    }

    private String getChatId(String userId1, String userId2) {
        // Создаем уникальный ID чата, сортируя ID пользователей
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }
}