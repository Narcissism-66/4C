package com.example.backend.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Log {
    private Integer id,userId;
    private String type,content;
    private Date time;

    public Log(Integer userId, String type, String content,Date time) {
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.time = time;
    }

    public Log(Integer id,Integer userId, String type, String content,Date time) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.time = time;
    }
}
