package com.example.backend.controller;

import com.example.backend.entity.Function;
import com.example.backend.entity.Log;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.User;
import com.example.backend.protect.JWTUtil;
import com.example.backend.service.FunctionService;
import com.example.backend.service.LogService;
import com.example.backend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    UserService userService;
    @Resource
    FunctionService functionService;
    @Resource
    LogService logService;

    @PostMapping("register")
    public RestBean<String> RegisterUser(@RequestParam("username") String username,
                                         @RequestParam("password") String password,
                                         @RequestParam("email") String email,
                                         @RequestParam("account") String account) {
        if(userService.checkAccountExists(account)==0) {
            int statue=userService.insertUser(new User(username, password, email, account));
            int a=functionService.addFunctionCount(new Function(userService.login(account,password).getId(),0,0,0,0));
            if (statue>0&&a>0)return RestBean.success("注册成功！");
            else return RestBean.failure(503,"注册失败！");
        }
        else return RestBean.failure(503,"账号已经被注册了，请重新选择账号！");
    }

    @GetMapping("login")
    public RestBean<String> LoginUser(@RequestParam("account") String account,
                                     @RequestParam("password") String password) {
        User user=userService.login(account,password);
        if(user!=null)
        {
            int a=logService.insertLog(new Log(user.getId(),"登陆操作","Id: "+user.getId()+" 的用户登陆了网站！",new Date()));
            return RestBean.success("登陆成功！", JWTUtil.createToken(user));
        }
        else {
            return RestBean.failure(401,"账号或密码不正确！");
        }
    }
}
