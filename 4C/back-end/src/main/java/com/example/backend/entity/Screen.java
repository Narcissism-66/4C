package com.example.backend.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Screen {
    private Integer id,userId;
    private String type,position,data,password;
    private Date time;

    public Screen(Integer userId, String type,String data,Date time, String position) {
        this.userId = userId;
        this.type = type;
        this.data = data;
        this.position = position;
        this.time = time;
    }

    public Screen(Integer id,Integer userId, String type,String data,Date time, String position,String password) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.data = data;
        this.position = position;
        this.time = time;
        this.password = password;
    }
}
