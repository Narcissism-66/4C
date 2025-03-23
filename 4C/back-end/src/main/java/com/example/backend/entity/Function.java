package com.example.backend.entity;

import lombok.Data;

@Data
public class Function {
    private Integer id,userId,single,merge,AI,report;
    public Function(Integer userId,Integer single,Integer merge,Integer AI,Integer report)
    {
        this.userId = userId;
        this.single = single;
        this.merge = merge;
        this.AI = AI;
        this.report = report;
    }
}
