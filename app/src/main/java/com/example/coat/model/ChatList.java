package com.example.coat.model;

public class ChatList {
    String id; //we wil need this id to get chat list, sender/receiver uid

    public ChatList(String id) {
        this.id = id;
    }

    public ChatList() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
