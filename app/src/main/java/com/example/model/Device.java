package com.example.model;

public class Device {
    private int id;
    private String name;
    private String serialNumber;
    private String rfid;
    private String status;
    private String purchaseDate;
    private String warrantyExpirationDate;
    private String location;
    private int activityId;

    public Device(int id, String name, String serialNumber, String rfid, String status, String purchaseDate, String warrantyExpirationDate, String location, int activityId) {
        this.id = id;
        this.name = name;
        this.serialNumber = serialNumber;
        this.rfid = rfid;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.warrantyExpirationDate = warrantyExpirationDate;
        this.location = location;
        this.activityId = activityId;
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

    public String getStatus() {
        return status;
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
}

