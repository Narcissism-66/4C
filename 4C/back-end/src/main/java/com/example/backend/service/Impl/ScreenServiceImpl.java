package com.example.backend.service.Impl;

import com.example.backend.entity.Screen;
import com.example.backend.mapper.ScreenMapper;
import com.example.backend.service.ScreenService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ScreenServiceImpl implements ScreenService {
    @Resource
    ScreenMapper screenMapper;


    @Override
    public Integer addScreen(Screen screen) {
        return screenMapper.addScreen(screen);
    }

    @Override
    public Screen checkPosition(Integer userId,String position) {
        return screenMapper.checkPosition(userId,position);
    }

    @Override
    public Integer updateScreen(Screen screen) {
        return screenMapper.updateScreen(screen);
    }

    @Override
    public Screen getScreenByUserId(Integer userId, String position) {
        return screenMapper.getScreenByUserId(userId,position);
    }

    @Override
    public Integer updatePassword(Integer userId, String password) {
        return screenMapper.updatePassword(userId,password);
    }

    @Override
    public Screen getScreenByUserIdAndPositionAndPassword(Integer userId, String position, String password) {
        return screenMapper.getScreenByUserIdAndPositionAndPassword(userId,position,password);
    }

}
