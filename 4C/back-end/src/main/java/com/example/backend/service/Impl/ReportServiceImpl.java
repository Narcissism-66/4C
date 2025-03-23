package com.example.backend.service.Impl;

import com.example.backend.entity.Report;
import com.example.backend.mapper.ReportMapper;
import com.example.backend.service.ReportService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Resource
    ReportMapper reportMapper;

    @Override
    public int insertReport(Report report) {
        return reportMapper.insertReport(report);
    }

    @Override
    public List<Report> getAllReportAndShow(Integer userId) {
        return reportMapper.getAllReportAndShow(userId);
    }

    @Override
    public List<Report> getAllReport(Integer userId) {
        return reportMapper.getAllReport(userId);
    }

    @Override
    public Report getReportById(Integer id) {
        return reportMapper.getReportById(id);
    }

    @Override
    public int updateReportShow(int id, String isshow) {
        return reportMapper.updateReportShow(id, isshow);
    }

    @Override
    public Report getLastReport(Integer userId) {
        return reportMapper.getLastReport(userId);
    }

    @Override
    public int getTotalReportCount(Integer userId) {
        return reportMapper.getTotalReportCount(userId);
    }

    @Override
    public int updatePassword(Integer id, String password) {
        return reportMapper.updatePassword(id, password);
    }

    @Override
    public Report getReportByIdAndPassword(Integer id, String password) {
        return reportMapper.getReportByIdAndPassword(id, password);
    }
}
