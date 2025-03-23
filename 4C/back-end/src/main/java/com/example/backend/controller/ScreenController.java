package com.example.backend.controller;

import com.example.backend.entity.RestBean;
import com.example.backend.entity.Screen;
import com.example.backend.service.ScreenService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/screen")
public class ScreenController {

    @Resource
    ScreenService screenService;

    @PostMapping("/addScreen")
    public RestBean<String> addScreen(@RequestParam("type") String type,
                                      @RequestParam("data") String data,
                                      @RequestParam("position") String position,
                                      HttpServletRequest request)
    {
        Integer userId=(Integer) request.getAttribute("id");
        Screen screen=new Screen(userId,type,data,new Date(),position);
        int status=screenService.addScreen(screen);
        if(status>0){
            return  RestBean.success("添加成功");
        }else {
            return  RestBean.failure(500,"添加失败");
        }
    }

    @GetMapping("/checkPosition")
    public RestBean<Screen> checkPosition(@RequestParam("position") String position,
                                           HttpServletRequest request)
    {
        Integer userId=(Integer) request.getAttribute("id");
        Screen screen=screenService.checkPosition(userId,position);
        return RestBean.success("cg",screen);
    }

    @PostMapping("/updateScreen")
    public RestBean<String> updateScreen(@RequestParam("type") String type,
                                          @RequestParam("data") String data,
                                          @RequestParam("position") String position,
                                         HttpServletRequest request){
        Integer userId=(Integer) request.getAttribute("id");
        Screen screen=new Screen(userId,type,data,new Date(),position);
        int status=screenService.updateScreen(screen);
        if(status>0){
            return  RestBean.success("修改成功");
        } else {
            return  RestBean.failure(500,"修改失败");
        }
    }

    @GetMapping("/getScreenByUserId")
    public RestBean<Screen> getScreenByUserId(@RequestParam("position") String position,
                                           HttpServletRequest request){
        Integer userId=(Integer) request.getAttribute("id");
        Screen screen=screenService.getScreenByUserId(userId,position);
        return RestBean.success("cg",screen);
    }

    @PostMapping("/updatePassword")
    public RestBean<String> updatePassword(@RequestParam("password") String password,
                                            HttpServletRequest request){
        Integer userId=(Integer) request.getAttribute("id");
        int status=screenService.updatePassword(userId,password);
        if(status>0){
            return  RestBean.success("修改成功");
        } else {
            return RestBean.failure(500, "修改失败");
        }
    }

    @GetMapping("/getScreenByUserIdAndPositionAndPassword")
    public RestBean<Screen> getScreenByUserIdAndPositionAndPassword(@RequestParam("position") String position,
                                            @RequestParam("password") String password,
                                            HttpServletRequest request){
        Integer userId=(Integer) request.getAttribute("id");
        Screen screen=screenService.getScreenByUserIdAndPositionAndPassword(userId,position,password);
        return RestBean.success("cg",screen);
    }
}
