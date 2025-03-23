package com.example.backend.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Feedback {
    private Integer id,userId,evaluate;
    private String title,content,type,username, response;
    private Date time;

    public Feedback(Integer userId, String title, String content, Date time, String type, Integer evaluate) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.time = time;
        this.type = type;
        this.evaluate = evaluate;
    }

    public Feedback(Integer id,Integer userId, String title, String content, Date time, String type, Integer evaluate,String response) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.time = time;
        this.type = type;
        this.evaluate = evaluate;
        this.response = response;
    }
    public Feedback(Integer id, Integer userId, String title, String content, Date time, String type, Integer evaluate) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.time = time;
        this.type = type;
        this.evaluate = evaluate;
    }
}
