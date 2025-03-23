package com.example.backend.mapper;

import com.example.backend.entity.Screen;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ScreenMapper {
    //添加一条记录
    @Insert("INSERT INTO screen(userId,type,data,time,position) VALUES (#{userId},#{type},#{data},#{time},#{position})")
    Integer addScreen(Screen screen);

    //检测当前用户对应的位置是否存在
    @Select("SELECT * FROM screen WHERE userId = #{userId} AND position = #{position}")
    Screen checkPosition(Integer userId,String position);


    //更新数据
    @Update("UPDATE screen SET data = #{data}, time = #{time}, type=#{type} WHERE userId = #{userId} AND position = #{position} ")
    Integer updateScreen(Screen screen);

    //通过position和id获取数据
    @Select("SELECT * FROM screen WHERE userId = #{userId} AND position = #{position} ")
    Screen getScreenByUserId(Integer userId, String position);

    //更新密码
    @Update("UPDATE screen SET password = #{password} WHERE userId = #{userId}")
    Integer updatePassword(Integer userId, String password);

    //通过position和id和password获取数据
    @Select("SELECT * FROM screen WHERE userId = #{userId} AND position = #{position} AND password = #{password}")
    Screen getScreenByUserIdAndPositionAndPassword(Integer userId, String position, String password);
}
