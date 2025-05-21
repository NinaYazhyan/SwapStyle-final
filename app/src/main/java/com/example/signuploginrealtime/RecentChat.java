package com.example.signuploginrealtime;

public class RecentChat {
    private String userId;
    private String userName;
    private String lastMessage;
    private long timestamp;

    public RecentChat() {} // Required for Firebase

    public RecentChat(String userId, String userName, String lastMessage, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
