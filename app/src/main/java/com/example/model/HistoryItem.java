package com.example.model;

import java.io.Serializable;
import java.util.HashMap;

public class HistoryItem implements Serializable{
    private HashMap<String,Integer> deviceDetail = new HashMap<String, Integer>();
    private String activityCode;
    private String activityDate;
    private String activityTime;
    private int userId;
    private String device;


    public HistoryItem(String activityCode, String activityDate, String activityTime, int userId, String device, HashMap<String, Integer> deviceDetail) {
        this.activityCode = activityCode;
        this.activityDate = activityDate;
        this.activityTime = activityTime;
        this.userId = userId;
        this.device = device;
        this.deviceDetail = deviceDetail;
    }

    public String getActivityCode() {
        return activityCode;
    }

    public String getActivityDate() {
        return activityDate;
    }

    public String getActivityTime() {
        return activityTime;
    }

    public int getUserId() {
        return userId;
    }

    public String getDevice() {
        return device;
    }

    public HashMap<String, Integer> getDeviceDetail() {
        return deviceDetail;
    }

    public void setDeviceDetail(HashMap<String, Integer> deviceDetail) {
        this.deviceDetail = deviceDetail;
    }

    public void setActivityDate(String activityDate) {
        this.activityDate = activityDate;
    }
}
