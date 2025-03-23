package com.example.backend.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Report {
    private Integer id,userId;
    private String data,analyse;
    private String filename,starttime,endtime,type,isshow,password;
    private Date time;

    public Report(int userId, String filename, Date time,String starttime, String endtime, String data, String analyse,String type,String isshow,String password) {
        this.userId = userId;
        this.filename = filename;
        this.starttime = starttime;
        this.endtime = endtime;
        this.data = data;
        this.analyse = analyse;
        this.time = time;
        this.type=type;
        this.isshow=isshow;
        this.password=password;
    }
}
