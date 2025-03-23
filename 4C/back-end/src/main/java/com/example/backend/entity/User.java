package com.example.backend.entity;

import lombok.Data;

@Data
public class User {
    private String username, password,account,avatar,email,role,bg,bgcolor;
    private Integer id;
    private Integer usageTime;


    public User(String username, String password, String email, String account) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.account = account;
    }
    public User( Integer id,String username, String password, String account,String avatar, String email,Integer usageTime,String role,String bg,String bgcolor) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.avatar=avatar;
        this.account = account;
        this.usageTime = usageTime;
        this.role = role;
        this.bg = bg;
        this.bgcolor = bgcolor;
    }

    public User(Integer id,String avatar,String username,String email)
    {
        this.id = id;
        this.avatar = avatar;
        this.username = username;
        this.email = email;
    }
    public User(Integer id,String role){
        this.id = id;
        this.role = role;
    }
}
