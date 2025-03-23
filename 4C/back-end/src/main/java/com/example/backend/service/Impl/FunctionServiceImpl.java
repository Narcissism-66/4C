package com.example.backend.service.Impl;

import com.example.backend.entity.Function;
import com.example.backend.entity.FunctionCountTotal;
import com.example.backend.mapper.FunctionMapper;
import com.example.backend.service.FunctionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class FunctionServiceImpl implements FunctionService {

    @Resource
    FunctionMapper functionMapper;

    @Override
    public int addFunctionCount(Function function) {
        return functionMapper.addFunctionCount(function);
    }

    @Override
    public int updateSingle(Integer userId) {
        return functionMapper.updateSingle(userId);
    }

    @Override
    public int updateMerge(Integer userId) {
        return functionMapper.updateMerge(userId);
    }

    @Override
    public int updateAI(Integer userId) {
        return functionMapper.updateAI(userId);
    }

    @Override
    public int updateReport(Integer userId) {
        return functionMapper.updateReport(userId);
    }

    @Override
    public FunctionCountTotal getFunctionCountTotal() {
        return functionMapper.getFunctionCountTotal();
    }
}
