package com.example.bloggery.model;

public class User {

    private String userName;
    private String email;
    private String profileImageUrl;

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public User(String userName, String email, String profileImageUrl) {
        this.userName = userName;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public User() {
    }
}
