package com.fiek.temadiplomes.Model;

import java.util.List;

public class User {
    private String email;
    private String username;
    private List<String> friends;
    private Boolean available;
    private String incomingVoice;
    private String incomingVideo;

    public User(){

    }

    public String getIncomingVoice() {
        return incomingVoice;
    }

    public void setIncomingVoice(String incomingVoice) {
        this.incomingVoice = incomingVoice;
    }

    public String getIncomingVideo() {
        return incomingVideo;
    }

    public void setIncomingVideo(String incomingVideo) {
        this.incomingVideo = incomingVideo;
    }

    public User(String email, String username, List<String> friends, Boolean available, String incomingVoice, String incomingVideo) {
        this.email = email;
        this.username = username;
        this.friends = friends;
        this.available = available;
        this.incomingVoice = incomingVoice;
        this.incomingVideo = incomingVideo;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }
}
