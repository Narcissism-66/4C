package com.example.backend.service.Impl;

import com.example.backend.entity.Log;
import com.example.backend.mapper.LogMapper;
import com.example.backend.service.LogService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {
    @Resource
    LogMapper logMapper;

    @Override
    public int insertLog(Log log) {
        return logMapper.insertLog(log);
    }

    @Override
    public List<Log> selectLogsByUserId(Integer userId) {
        return logMapper.selectLogsByUserId(userId);
    }

    @Override
    public List<Log> selectAllLogs() {
        return logMapper.selectAllLogs();
    }

    // 每周一凌晨 0 点清空日志
    @Scheduled(cron = "0 0 0 ? * MON")
    public void clearLogsWeekly() {
        logMapper.clearLogs();
        System.out.println("已清空 system_log 表中的数据！");
    }
}
