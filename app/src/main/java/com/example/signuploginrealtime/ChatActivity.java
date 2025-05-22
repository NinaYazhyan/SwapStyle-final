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
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messages;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ImageButton backButton;
    private TextView userNameTextView;

    private FirebaseUser currentUser;
    private DatabaseReference messagesRef;
    private DatabaseReference recentChatsRef;

    private String receiverId;
    private String receiverName;

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

        // Получаем ID и имя получателя из Intent
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("userName");

        if (receiverId == null) {
            Toast.makeText(this, "Ошибка: Информация о пользователе отсутствует", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализируем Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        recentChatsRef = FirebaseDatabase.getInstance().getReference("recent_chats");

        // Инициализируем UI элементы
        recyclerView = findViewById(R.id.recycler_view_messages);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        backButton = findViewById(R.id.back_button);
        userNameTextView = findViewById(R.id.user_name);

        // Устанавливаем имя пользователя в заголовке
        userNameTextView.setText(receiverName);

        // Настраиваем RecyclerView
        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages, currentUser.getUid());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Загружаем сообщения
        loadMessages();

        // Настраиваем обработчики событий
        sendButton.setOnClickListener(v -> sendMessage());
        backButton.setOnClickListener(v -> finish());
    }

    private void loadMessages() {
        String chatId = getChatId(currentUser.getUid(), receiverId);
        messagesRef.child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        messages.add(message);
                    }
                }
                adapter.notifyDataSetChanged();
                scrollToBottom();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Ошибка при загрузке сообщений: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        String chatId = getChatId(currentUser.getUid(), receiverId);
        long timestamp = System.currentTimeMillis();
        Message message = new Message(currentUser.getUid(), receiverId, messageText, timestamp);

        // Сохраняем сообщение
        messagesRef.child(chatId).push().setValue(message).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageEditText.setText("");
                updateRecentChats(message);
            } else {
                Toast.makeText(ChatActivity.this, "Не удалось отправить сообщение", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecentChats(Message message) {
        // Получаем информацию о текущем пользователе
        FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String senderName = snapshot.child("name").getValue(String.class);
                        if (senderName == null) senderName = "Пользователь";

                        // Обновляем последние чаты для обоих пользователей
                        RecentChat senderRecentChat = new RecentChat(receiverId, receiverName, message.getMessageText(), message.getTimestamp());
                        RecentChat receiverRecentChat = new RecentChat(currentUser.getUid(), senderName, message.getMessageText(), message.getTimestamp());

                        recentChatsRef.child(currentUser.getUid()).child(receiverId).setValue(senderRecentChat);
                        recentChatsRef.child(receiverId).child(currentUser.getUid()).setValue(receiverRecentChat);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, "Ошибка обновления чатов", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getChatId(String uid1, String uid2) {
        // Создаем уникальный ID чата, сортируя ID пользователей
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            recyclerView.post(() -> recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1));
        }
    }
}