package com.example.backend.controller;

import com.example.backend.entity.RestBean;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")

public class UserController {
    @Resource
    UserService userService;

    @GetMapping("information")
    public RestBean<User> getMyInfo(HttpServletRequest request){//没有正确的令牌的话进不去，所以不用判断是不是空了
//        if(session.getAttribute("user")==null)return RestBean.failure(401,"未登陆~");
        User theUser=userService.getUserById((Integer) request.getAttribute("id"));
        theUser.setPassword(null);
        //获取键为user的值
        return RestBean.success("获取成功！",theUser);
    }

    @GetMapping("/getUserById")
    public RestBean<User> getUserById(@RequestParam("id") Integer id){
        User theUser=userService.getUserById(id);
        theUser.setPassword(null);
        return RestBean.success("获取成功！",theUser);
    }


    @Value("${file.upload-dir}") // 从配置文件中读取上传目录路径
    private String uploadDir;

    @PostMapping("/uploadImg")
    public RestBean<String> uploadImage(@RequestParam("image") MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return RestBean.failure(400, "上传的文件为空");
            }

            // 创建上传目录（如果不存在）
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                if (!directory.mkdirs()) { // 创建目录
                    return RestBean.failure(500, "无法创建上传目录");
                }
            }

            // 生成唯一的文件名
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            // 将文件保存到服务器
            Files.copy(file.getInputStream(), filePath);

            // 返回成功响应，包含文件的相对路径
            String fileUrl = "uploads/" + fileName; // 相对路径
            return RestBean.success("文件上传成功", fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return RestBean.failure(500, "文件上传失败：" + e.getMessage());
        }
    }


    @PostMapping("/updateUser")
    public RestBean<String> updateUser(@RequestParam("avatar") String avatar,
                                     @RequestParam("username") String username,
                                     @RequestParam("email") String email,
                                     HttpServletRequest request){
        int userId= (int) request.getAttribute("id");
        User user = new User(userId,avatar,username,email);
        int a=userService.updateUser(user);
        if (a!=0)
        return RestBean.success("修改成功！");
        else return RestBean.failure(503,"出错了");
    }

    @PostMapping("/updateUsageTime")
    public RestBean<String> updateUsageTime(@RequestParam("usageTime") Integer usageTime,
                                            HttpServletRequest request){
        System.out.println(usageTime);
        Integer userId=(Integer) request.getAttribute("id");
        int a=userService.updateUsageTime(userId,usageTime);
        if (a!=0)return RestBean.success("cg");
        else return RestBean.failure(503,"失败");
    }

    @GetMapping("/getAllUsers")
    public RestBean<List<User>> getAllUsers()
    {
        return RestBean.success("cg",userService.getAllUsers());
    }

    @GetMapping("/getAllUsersInfo")
    public  RestBean<List<User>>getAllUsersInfo()
    {
        return RestBean.success("cg",userService.getAllUsersInfo());
    }

    @GetMapping("/searchUsersByName")
    public RestBean<List<User>> searchUsersByName(@RequestParam("username") String username)
    {
        return RestBean.success("cg",userService.searchUsersByName(username));
    }

    @PostMapping("/deleteUser")
    public RestBean<String> deleteUser(@RequestParam("id") Integer id)
    {
        int a=userService.deleteUser(id);
        if (a!=0) return RestBean.success("cg");
        else return RestBean.failure(503,"失败");
    }

    @PostMapping("/updateUserRole")
    public RestBean<String> updateUserRole(@RequestParam("id") Integer id,
                                           @RequestParam("role") String role) {
        int a=userService.updateUserRole(id, role);
        if (a!=0) return RestBean.success("cg");
        else return RestBean.failure(503,"失败");
    }

    @PostMapping("/updateUserBg")
    public RestBean<String> updateUserBg(@RequestParam("id") Integer id,
                                           @RequestParam("bg") String bg) {
        int a=userService.updateUserBg(id, bg);
        if (a!=0) return RestBean.success("cg");
        else return RestBean.failure(503,"失败");
    }

    @PostMapping("/updateUserBgColor")
    public RestBean<String> updateUserBgColor(@RequestParam("id") Integer id,
                                           @RequestParam("bgColor") String bgColor) {
        int a=userService.updateUserBgColor(id, bgColor);
        if (a!=0) return RestBean.success("cg");
        else return RestBean.failure(503,"失败");
    }
}
