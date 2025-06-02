package com.example.kurssovai;

import java.util.List;

public class Doll {
    private String id;
    private String userId;
    private String baseDoll;
    private List<String> clothingLayers;
    private String printCode;

    public Doll() {
        // Пустой конструктор для Firestore
    }

    public Doll(String userId, String baseDoll, List<String> clothingLayers) {
        this.userId = userId;
        this.baseDoll = baseDoll;
        this.clothingLayers = clothingLayers;
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBaseDoll() {
        return baseDoll;
    }

    public void setBaseDoll(String baseDoll) {
        this.baseDoll = baseDoll;
    }

    public List<String> getClothingLayers() {
        return clothingLayers;
    }

    public void setClothingLayers(List<String> clothingLayers) {
        this.clothingLayers = clothingLayers;
    }

    public String getPrintCode() {
        return printCode;
    }

    public void setPrintCode(String printCode) {
        this.printCode = printCode;
    }
}