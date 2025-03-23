package com.example.backend.service;

import com.example.backend.entity.Feedback;

import java.util.List;
import java.util.Map;

public interface FeedbackService {
    int addFeedback(Feedback feedback);
    List<Feedback> getFeedbackList();
    List<Map<String, Integer>>getFeedbackCountByType();
    List<Feedback>getFeedbackByUserId(int userId);
    int solveFeedback(int id,String response);
}
