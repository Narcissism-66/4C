package com.example.backend.controller;

import com.example.backend.entity.Log;
import com.example.backend.entity.RestBean;
import com.example.backend.service.LogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/log")
public class LogController {
    @Resource
    LogService logService;

    @PostMapping("/insertLog")
    public RestBean<String> insertLog(HttpServletRequest request,
                                      @RequestParam("type")String type,
                                      @RequestParam("content")String content){
        int b= logService.insertLog(new Log((Integer)request.getAttribute("id"),type,content,new Date()));
        if (b>0)
        return RestBean.success("cg");
        else return RestBean.failure(503,"错误");
    }

    @GetMapping("/selectLogsByUserId")
    public RestBean<List<Log>> selectLogsByUserId(HttpServletRequest request){
        return RestBean.success("cg",logService.selectLogsByUserId((Integer) request.getAttribute("id")));
    }

    @GetMapping("/selectAllLogs")
    public RestBean<List<Log>>selectAllLogs()
    {
        return RestBean.success("cg",logService.selectAllLogs());
    }
}
