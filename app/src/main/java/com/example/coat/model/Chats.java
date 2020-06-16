package com.example.coat.model;

public class Chats {
    String message;
    String receiver;
    String sender;
    String timeStamp;
    String type;
    boolean isSeen;

    public Chats() {
    }

    public Chats(String message, String receiver, String sender, String timeStamp,boolean isSeen,String type) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timeStamp = timeStamp;
        this.isSeen=isSeen;
        this.type=type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
