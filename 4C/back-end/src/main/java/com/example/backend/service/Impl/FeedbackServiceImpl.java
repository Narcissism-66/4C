package com.example.backend.service.Impl;

import com.example.backend.entity.Feedback;
import com.example.backend.entity.User;
import com.example.backend.mapper.FeedbackMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.FeedbackService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Resource
    FeedbackMapper feedbackMapper;
    @Resource
    UserMapper userMapper;

    @Override
    public int addFeedback(Feedback feedback) {
        int statue=feedbackMapper.addFeedback(feedback);
        if (statue!=0)return 1;
        else return 0;
    }

    @Override
    public List<Feedback> getFeedbackList() {
        List<Feedback> feedbackList = feedbackMapper.getFeedbackList();
        for(Feedback feedback :feedbackList)
        {
            feedback.setUsername(userMapper.getUserById(feedback.getUserId()).getUsername());
        }
        return feedbackList;
    }

    @Override
    public List<Map<String, Integer>> getFeedbackCountByType() {
        return feedbackMapper.getFeedbackCountByType();
    }

    @Override
    public List<Feedback> getFeedbackByUserId(int userId) {
        return feedbackMapper.getFeedbackByUserId(userId);
    }

    @Override
    public int solveFeedback(int id,String response) {
        return feedbackMapper.solveFeedback(id,response);
    }
}
