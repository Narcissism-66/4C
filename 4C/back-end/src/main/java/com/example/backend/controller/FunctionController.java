package com.example.backend.controller;

import com.example.backend.entity.FunctionCountTotal;
import com.example.backend.entity.RestBean;
import com.example.backend.service.FunctionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/function")
public class FunctionController {
    @Resource
    FunctionService functionService;

    @PostMapping("/updateSingle")
    public RestBean<String> updateSingle(HttpServletRequest request)
    {
        functionService.updateSingle((Integer) request.getAttribute("id"));
        return RestBean.success("cg");
    }

    @PostMapping("/updateMerge")
    public RestBean<String> updateMerge(HttpServletRequest request)
    {
        functionService.updateMerge((Integer) request.getAttribute("id"));
        return RestBean.success("cg");
    }

    @PostMapping("/updateAI")
    public RestBean<String> updateAI(HttpServletRequest request)
    {
        functionService.updateAI((Integer) request.getAttribute("id"));
        return RestBean.success("cg");
    }

    @PostMapping("/updateReport")
    public RestBean<String> updateReport(HttpServletRequest request)
    {
        functionService.updateReport((Integer) request.getAttribute("id"));
        return RestBean.success("cg");
    }

    @GetMapping("/getFunctionCountTotal")
    public RestBean<FunctionCountTotal> getFunctionCountTotal()
    {
        return RestBean.success("cg",functionService.getFunctionCountTotal());
    }
}
