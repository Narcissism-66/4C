package com.example.backend.service.Impl;

import com.example.backend.mapper.UserMapper;
import com.example.backend.service.UserService;
import com.example.backend.entity.User;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    UserMapper userMapper;

    //注册
    @Override
    public int insertUser(User user) {
        return userMapper.insertUser(user);
    }

    //登陆
    @Override
    public User login(String account, String password) {
        return userMapper.login(account, password);
    }

    //检查账号是否存在
    @Override
    public int checkAccountExists(String account) {
        return userMapper.checkAccountExists(account);
    }

    @Override
    public User getUserById(Integer id) {
        return userMapper.getUserById(id);
    }
}
