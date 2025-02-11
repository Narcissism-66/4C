package com.example.backend.service;

import com.example.backend.entity.User;

public interface UserService {
    int insertUser(User user);
    User login(String account,String password);
    int checkAccountExists(String account);
    User getUserById(Integer id);
}
