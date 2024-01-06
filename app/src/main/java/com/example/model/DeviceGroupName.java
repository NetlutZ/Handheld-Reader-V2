package com.example.model;

import java.io.Serializable;

public class DeviceGroupName implements Serializable {
    private int id;
    private String name;
    private int quantity;
    private int maxBorrowDate;
    private String img;

    public DeviceGroupName(int id, String name, int quantity, int maxBorrowDate, String img) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.maxBorrowDate = maxBorrowDate;
        this.img = img;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getMaxBorrowDate() {
        return maxBorrowDate;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImg() {
        return img;
    }
}
