package com.example.model;

import java.io.Serializable;

public class Device implements Serializable {
    private int id;
    private String name;
    private String serialNumber;
    private String rfid;
    private String rfidStatus;
    private String purchaseDate;
    private String warrantyExpirationDate;
    private String location;
    private int activityId;
    private int maxBorrowDate;
    private String image;

    public Device(int id, String name, String serialNumber, String rfid, String rfidStatus, String purchaseDate, String warrantyExpirationDate, String location, int activityId, int maxBorrowDate, String image) {
        this.id = id;
        this.name = name;
        this.serialNumber = serialNumber;
        this.rfid = rfid;
        this.rfidStatus = rfidStatus;
        this.purchaseDate = purchaseDate;
        this.warrantyExpirationDate = warrantyExpirationDate;
        this.location = location;
        this.activityId = activityId;
        this.maxBorrowDate = 1;
        this.image = image;
    }

    public Device() {

    }

    public Device(String rfid) {
        this.rfid = rfid;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getRfid() {
        return rfid;
    }

    public String getRfidStatus() {
        return rfidStatus;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public String getWarrantyExpirationDate() {
        return warrantyExpirationDate;
    }

    public String getLocation() {
        return location;
    }

    public int getActivityId() {
        return activityId;
    }

    public int getMaxBorrowDate() {
        return maxBorrowDate;
    }

    public String getImg() {
        return image;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

