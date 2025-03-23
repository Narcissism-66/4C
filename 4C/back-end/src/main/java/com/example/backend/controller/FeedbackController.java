package com.example.backend.controller;

import com.example.backend.entity.Feedback;
import com.example.backend.entity.RestBean;
import com.example.backend.service.FeedbackService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Resource
    FeedbackService feedbackService;

    @PostMapping("/addFeedback")
    public RestBean<String> addFeedback(@RequestParam("userId") Integer userId,
                                        @RequestParam("title") String title,
                                        @RequestParam("content") String content,
                                        @RequestParam("type") String type ,
                                        @RequestParam("evaluate") Integer evaluate) {
        Feedback feedback=new Feedback(userId, title, content,new Date(),type, evaluate);
        int status=feedbackService.addFeedback(feedback);
        if(status==1) return RestBean.success("提交反馈成功！");
        else return RestBean.failure(503,"出错了，请联系管理员！");
    }

    @GetMapping("/getFeedbackList")
    public RestBean<List<Feedback>> getFeedbackList()
    {
        System.out.println(feedbackService.getFeedbackList());
        return RestBean.success("获取成功！",feedbackService.getFeedbackList());
    }

    @GetMapping("getFeedbackCountByType")
    public RestBean<List<Map<String, Integer>>> getFeedbackCountByType()
    {
        return RestBean.success("cg",feedbackService.getFeedbackCountByType());
    }

    @GetMapping("/getFeedbackByUserId")
    public RestBean<List<Feedback>> getFeedbackByUserId(HttpServletRequest request)
    {
//        System.out.println(feedbackService.getFeedbackList());
        return RestBean.success("获取成功！",feedbackService.getFeedbackByUserId((Integer)request.getAttribute("id")));
    }

    @PostMapping("/solveFeedback")
    public RestBean<String> solveFeedback(@RequestParam("id") Integer id,
                                          @RequestParam("response") String response)
    {
        int status=feedbackService.solveFeedback(id,response);
        if (status==1)
        return RestBean.success("cg");
        else return RestBean.failure(503,"错误");
    }
}
