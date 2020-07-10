package com.example.coat.model;

public class BookingSession {
    private String time;
    private String userId;
    private String psychologistId;
    private String status;
    private String timeStamp;
    boolean isBlocked = false;

    public BookingSession() {
    }

    public BookingSession(String time, String userId, String psychologistId, String status, String timeStamp, boolean isBlocked) {
        this.time = time;
        this.userId = userId;
        this.psychologistId = psychologistId;
        this.status = status;
        this.timeStamp = timeStamp;
        this.isBlocked = isBlocked;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPsychologistId() {
        return psychologistId;
    }

    public void setPsychologistId(String psychologistId) {
        this.psychologistId = psychologistId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
