package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

public class ChatFragment extends Fragment implements RecentChatAdapter.OnChatClickListener {

    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private RecentChatAdapter adapter;
    private List<RecentChat> chatList;
    private FirebaseUser currentUser;
    private DatabaseReference recentChatsRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            return view;
        }

        recyclerView = view.findViewById(R.id.recycler_view_chats);
        emptyTextView = view.findViewById(R.id.empty_state_text);
        FloatingActionButton fabNewChat = view.findViewById(R.id.fab_new_chat);

        chatList = new ArrayList<>();
        adapter = new RecentChatAdapter(chatList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        recentChatsRef = FirebaseDatabase.getInstance().getReference("recent_chats")
                .child(currentUser.getUid());

        loadChats();

        fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), UserSearchActivity.class))
        );

        return view;
    }

    private void loadChats() {
        recentChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    RecentChat chat = chatSnapshot.getValue(RecentChat.class);
                    if (chat != null) {
                        chatList.add(chat);
                    }
                }

                // Сортировка по времени (последние сверху)
                Collections.sort(chatList, (c1, c2) ->
                        Long.compare(c2.getTimestamp(), c1.getTimestamp())
                );

                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка ошибки
            }
        });
    }

    private void updateEmptyState() {
        if (chatList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onChatClick(RecentChat chat) {
        ChatActivity.start(getContext(), chat.getUserId(), chat.getUserName());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.updateCurrentDay();
        }
    }
}