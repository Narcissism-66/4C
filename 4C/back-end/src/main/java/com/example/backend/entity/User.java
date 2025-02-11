package com.example.backend.entity;

import lombok.Data;

@Data
public class User {
    private String username, password,account,avatar,email;
    private Integer id;

    public User(String username, String password, String email, String account) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.account = account;
    }
}
