package com.example.bloggery.model;

import com.google.firebase.firestore.Exclude;

public class Item {

    private String photoUrl;
    private String price;
    private String store;
    private String title;
    private String author;
    private String emailAuthor;
    @Exclude
    public String documentId;
    @Exclude
    public String category;

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getPrice() {
        return price;
    }

    public String getStore() {
        return store;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getEmailAuthor() {
        return emailAuthor;
    }

    public Item(String author, String title, String store, String price, String photoUrl, String emailAuthor) {
        this.author = author;
        this.title = title;
        this.store = store;
        this.price = price;
        this.photoUrl = photoUrl;
        this.emailAuthor = emailAuthor;
    }


    public Item() {
    }
}
