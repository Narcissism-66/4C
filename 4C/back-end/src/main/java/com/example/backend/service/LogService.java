package com.example.backend.service;

import com.example.backend.entity.Log;

import java.util.List;

public interface LogService {
    int insertLog(Log log);
    List<Log> selectLogsByUserId(Integer userId);
    List<Log> selectAllLogs();
}
