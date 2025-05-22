package com.example.signuploginrealtime;

public class WardrobeItem {
    private String id;
    private String imageUrl;
    private String title;
    private String description;
    private String category;
    private String subcategory;
    private String size;
    private String userId; // To associate items with users
    private String userName; // Имя владельца предмета

    // Required empty constructor for Firebase
    public WardrobeItem() {}

    public WardrobeItem(String id, String imageUrl, String title, String description,
                        String category, String subcategory, String size, String userId, String userName) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.description = description;
        this.category = category;
        this.subcategory = subcategory;
        this.size = size;
        this.userId = userId;
        this.userName = userName;
    }

    // Существующие геттеры и сеттеры...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    // Новые методы для имени пользователя
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}