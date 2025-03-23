package com.example.backend.service;

import com.example.backend.entity.Function;
import com.example.backend.entity.FunctionCountTotal;

public interface FunctionService {
    int addFunctionCount(Function function);
    int updateSingle(Integer userId);
    int updateMerge(Integer userId);
    int updateAI(Integer userId);
    int updateReport(Integer userId);
    FunctionCountTotal getFunctionCountTotal();
}
