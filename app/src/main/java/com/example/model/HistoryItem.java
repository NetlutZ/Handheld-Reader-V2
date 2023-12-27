package com.example.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;

public class HistoryItem {
    private HashMap<String,Integer> deviceDetail = new HashMap<String, Integer>();
    private LocalDate date;
    private LocalTime time;
    private String status;

    public HistoryItem(HashMap<String, Integer> deviceDetail, LocalDate date, LocalTime time, String status) {
        this.deviceDetail = deviceDetail;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public HashMap<String, Integer> getDeviceDetail() {
        return deviceDetail;
    }

    public void setDeviceDetail(HashMap<String, Integer> deviceDetail) {
        this.deviceDetail = deviceDetail;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
