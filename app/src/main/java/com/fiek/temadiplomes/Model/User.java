package com.fiek.temadiplomes.Model;

import java.util.List;

public class User {
    private String email;
    private String username;
    private List<String> friends;

    public User(){

    }

    public User(String email, String username, List<String> friends) {
        this.email = email;
        this.username = username;
        this.friends = friends;
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
