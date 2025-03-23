package com.example.backend.controller.AIController;

import com.example.backend.entity.RestBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class ChatAI {

    @Resource
    OpenAiChatModel openAiChatModel;

    @PostMapping("/chat")
    public RestBean<String> chatWithAI(@RequestParam("message") String message)
    {
        String text=openAiChatModel.call(message);
        System.out.println(text);
        if (text==null) return  RestBean.failure(503,"出错了");
        else return RestBean.success(text);
    }
}
