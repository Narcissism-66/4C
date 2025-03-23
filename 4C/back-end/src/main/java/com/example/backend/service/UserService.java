package com.example.backend.service;

import com.example.backend.entity.User;

import java.util.List;

public interface UserService {
    int insertUser(User user);
    User login(String account,String password);
    int checkAccountExists(String account);
    User getUserById(Integer id);
    int updateUser(User user);
    int updateUsageTime(Integer userId,Integer usageTime);
    List<User>getAllUsers();
    List<User> getAllUsersInfo();
    int updateUserRole(Integer id, String role);
    int deleteUser(Integer id);
    List<User> searchUsersByName(String username);
    int updateUserBg(Integer id, String bg);
    int updateUserBgColor(Integer id, String bgColor);
}
