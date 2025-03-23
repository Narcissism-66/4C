package com.example.backend.mapper;

import com.example.backend.entity.Log;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LogMapper {
    //添加一条日志
    @Insert("INSERT INTO system_log(userId,type,content,time) VALUES (#{userId},#{type},#{content},#{time})")
    int insertLog(Log log);


    //获取某一个用户的日志信息
    @Select("SELECT * FROM system_log WHERE userId = #{userId} ORDER BY time DESC")
    List<Log> selectLogsByUserId(Integer userId);

    //获取所有人的日志信息
    @Select("SELECT * FROM system_log ORDER BY time DESC")
    List<Log> selectAllLogs();

    //定时清空数据
    @Delete("DELETE FROM system_log")
    void clearLogs();
}

