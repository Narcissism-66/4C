package com.example.backend.service;

import com.example.backend.entity.Screen;

public interface ScreenService {
    Integer addScreen(Screen screen);
    Screen checkPosition(Integer userId,String position);
    Integer updateScreen(Screen screen);
    Screen getScreenByUserId(Integer userId, String position);
    Integer updatePassword(Integer userId, String password);
    Screen getScreenByUserIdAndPositionAndPassword(Integer userId, String position, String password);
}
