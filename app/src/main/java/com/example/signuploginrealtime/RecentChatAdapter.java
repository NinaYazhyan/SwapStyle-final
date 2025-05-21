package com.example.signuploginrealtime;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentChatAdapter extends RecyclerView.Adapter<RecentChatAdapter.ChatViewHolder> {

    private final List<RecentChat> chatList;
    private final OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(RecentChat chat);
    }

    public RecentChatAdapter(List<RecentChat> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        RecentChat chat = chatList.get(position);
        holder.username.setText(chat.getUserName());
        holder.lastMessage.setText(chat.getLastMessage());

        String formattedTime = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(chat.getTimestamp()));
        holder.timestamp.setText(formattedTime);

        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView username, lastMessage, timestamp;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.chat_user_name);
            lastMessage = itemView.findViewById(R.id.chat_last_message);
            timestamp = itemView.findViewById(R.id.chat_timestamp);
        }
    }

    public void updateCurrentDay() {
        notifyDataSetChanged();
    }
}
