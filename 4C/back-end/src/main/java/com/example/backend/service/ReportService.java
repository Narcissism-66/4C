package com.example.backend.service;

import com.example.backend.entity.Report;

import java.util.List;

public interface ReportService {
    int insertReport(Report report);
    List<Report> getAllReportAndShow(Integer userId);
    List<Report> getAllReport(Integer userId);
    Report getReportById(Integer id);
    int updateReportShow(int id,String isshow);
    Report getLastReport(Integer userId);
    int getTotalReportCount(Integer userId);
    int updatePassword(Integer id, String password);
    Report getReportByIdAndPassword(Integer id, String password);
}
