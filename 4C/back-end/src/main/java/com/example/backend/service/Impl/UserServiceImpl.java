package com.example.backend.service.Impl;

import com.example.backend.mapper.UserMapper;
import com.example.backend.service.UserService;
import com.example.backend.entity.User;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public int updateUser(User user) {
        return userMapper.updateUser(user);
    }

    @Override
    public int updateUsageTime(Integer userId,Integer usageTime) {
        return userMapper.updateUsageTime(userId,usageTime);
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    @Override
    public List<User> getAllUsersInfo() {
        return userMapper.getAllUsersInfo();
    }

    @Override
    public int updateUserRole(Integer id, String role) {
        return userMapper.updateUserRole(id,role);
    }

    @Override
    public int deleteUser(Integer id) {
        return userMapper.deleteUser(id);
    }

    @Override
    public List<User> searchUsersByName(String username) {
        return userMapper.searchUsersByName(username);
    }

    @Override
    public int updateUserBg(Integer id, String bg) {
        return userMapper.updateUserBg(id,bg);
    }

    @Override
    public int updateUserBgColor(Integer id, String bgColor) {
        return userMapper.updateUserBgColor(id,bgColor);
    }
}
