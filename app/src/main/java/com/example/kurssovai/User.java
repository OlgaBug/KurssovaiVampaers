package com.example.kurssovai;


import java.util.List;

public class User {
    private String id;
    private List<String> dollIds;

    public User() {
        // Пустой конструктор для Firestore
    }

    public User(String id, List<String> dollIds) {
        this.id = id;
        this.dollIds = dollIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getDollIds() {
        return dollIds;
    }

    public void setDollIds(List<String> dollIds) {
        this.dollIds = dollIds;
    }
}