package com.example.signuploginrealtime;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class HelperClass {
    // Fields
    private String name;
    private String email;
    private String username;
    private String password;
    private String location;

    // Empty constructor (required for Firebase)
    public HelperClass() {
        this.name = "";
        this.email = "";
        this.username = "";
        this.password = "";
        this.location = "";
    }

    // Parameterized constructor
    public HelperClass(String name, String email, String username, String password, String location) {
        this.name = name != null ? name : "";
        this.email = email != null ? email : "";
        this.username = username != null ? username : "";
        this.password = password != null ? password : "";
        this.location = location != null ? location : "";
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username : "";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password != null ? password : "";
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location != null ? location : "";
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}