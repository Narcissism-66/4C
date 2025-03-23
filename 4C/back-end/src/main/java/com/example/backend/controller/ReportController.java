package com.example.backend.controller;

import com.example.backend.entity.Report;
import com.example.backend.entity.RestBean;
import com.example.backend.service.ReportService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Resource
    ReportService reportService;

    @PostMapping("/addReport")
    public RestBean<String> addReport(
                                      @RequestParam("filename")String filename,
                                      @RequestParam("starttime")String starttime,
                                      @RequestParam("endtime")String endtime,
                                      @RequestParam("data")String data,
                                      @RequestParam("analyse")String analyse,
                                      @RequestParam("type")String type,
                                      HttpServletRequest request) {
        System.out.println(filename);
        switch (type) {
            case "bar":
                // 处理 bar 图表
                type = "BarChart";
                break;
            case "line":
                // 处理 line 图表
                type = "LineChart";
                break;
            case "pie":
                // 处理 pie 图表
                type = "PieChart";
                break;
            case "scatter":
                // 处理 scatter 图表
                type = "ScatterChart";
                break;
            case "area":
                // 处理 area 图表
                type = "AreaChart";
                break;
            case "bubble":
                // 处理 bubble 图表
                type = "BubbleChart";
                break;
            case "box":
                // 处理 box 图表
                type = "BoxPlotChart";
                break;
            case "radar":
                // 处理 radar 图表
                type = "RadarChart";
                break;
            case "funnel":
                // 处理 funnel 图表
                type = "FunnelChart";
                break;
            case "treemap":
                // 处理 treemap 图表
                type = "TreemapChart";
                break;
            case "sankey":
                // 处理 sankey 图表
                type = "SankeyChart";
                break;
            case "dual":
                // 处理 dual 图表
                type = "DualAxisChart";
                break;
            case "heatmap":
                // 处理 heatmap 图表
                type = "HeatmapChart";
                break;
            case "wordcloud":
                // 处理 wordcloud 图表
                type = "WordCloud";
                break;
            case "relationship":
                // 处理 relationship 图表
                type = "RelationShipChart";
                break;
            default:
                // 处理未知图表类型
            break;
        }
        int userId = (int) request.getAttribute("id");
        int status=reportService.insertReport(new Report(userId,filename,new Date() ,starttime,endtime,data,analyse,type,"true",""));
        if(status==1)return  RestBean.success("保存成功！");
        else return RestBean.failure(503,"出错了");
    }


    @GetMapping("/getAllReportAndShow")
    public RestBean<List<Report>> getAllReportAndShow(HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute("id");
        return RestBean.success("成功！",reportService.getAllReportAndShow(userId));
    }
    @GetMapping("/getAllReport")
    public RestBean<List<Report>> getAllReport(HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute("id");
        return RestBean.success("成功！",reportService.getAllReport(userId));
    }


    @GetMapping("/viewReport")
    public  RestBean<Report> getReportById(@RequestParam("id") Integer id){
        return RestBean.success("成功",reportService.getReportById(id));
    }

    @PostMapping("/updateReportShow")
    public RestBean<String> updateReportShow(@RequestParam("id") Integer id,
                                             @RequestParam("isshow") String isshow){
        reportService.updateReportShow(id,isshow);
        return RestBean.success("成功");
    }

    @GetMapping("/getLastReport")
    public RestBean<Report> getLastReport(HttpServletRequest request){
        Integer userId= (Integer) request.getAttribute("id");

        Report report=reportService.getLastReport(userId);

        return RestBean.success("获取成功",report);
    }

    @GetMapping("/getTotalReportCount")
    public RestBean<Integer> getTotalReportCount(HttpServletRequest request){
        Integer userId= (Integer) request.getAttribute("id");
        return RestBean.success("获取成功",reportService.getTotalReportCount(userId));
    }

    @PostMapping("/MakeShare")
    public RestBean<String> MakeShare(@RequestParam("reportId") Integer reportId,
                                      @RequestParam("password") String password){
        reportService.updatePassword(reportId,password);
        return RestBean.success("成功");
    }
    @GetMapping("/getReportByIdAndPassword")
    public RestBean<Report> getReportByIdAndPassword(@RequestParam("reportId") Integer reportId,
                                                    @RequestParam("password") String password){
        Report report=reportService.getReportByIdAndPassword(reportId,password);
        if (report!=null)
        return RestBean.success("获取成功",report);
        else return RestBean.failure(503,"密码错误");
    }
}
