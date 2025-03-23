package com.example.backend.mapper;

import com.example.backend.entity.Function;
import com.example.backend.entity.FunctionCountTotal;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FunctionMapper {

    //添加一条记录
    @Insert("INSERT INTO function_count(userId,single,merge,AI,report)VALUES(#{userId},#{single},#{merge},#{AI},#{report})")
    int addFunctionCount(Function function);

    //更新single的值
    @Insert("UPDATE function_count SET single = single+1 WHERE userId = #{userId}")
    int updateSingle(Integer userId);

    //更新merge的值
    @Insert("UPDATE function_count SET merge = merge+1 WHERE userId = #{userId}")
    int updateMerge(Integer userId);

    //更新AI的值
    @Insert("UPDATE function_count SET AI = AI+1 WHERE userId = #{userId}")
    int updateAI(Integer userId);

    //更新report的值
    @Insert("UPDATE function_count SET report = report+1 WHERE userId = #{userId}")
    int updateReport(Integer userId);

    //每个功能的使用数量
    @Select("SELECT SUM(single) AS singleTotal, SUM(merge) AS mergeTotal, SUM(AI) AS AITotal, SUM(report) AS reportTotal FROM function_count ")
    FunctionCountTotal getFunctionCountTotal();

}
