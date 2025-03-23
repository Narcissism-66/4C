package com.example.backend.mapper;

import com.example.backend.entity.Feedback;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface FeedbackMapper {
    //添加反馈
    @Insert("INSERT INTO feedback(userId,title,content,time,type,evaluate) VALUES (#{userId},#{title},#{content},#{time},#{type},#{evaluate})")
    int addFeedback(Feedback feedback);

    //获取反馈信息
    @Select("SELECT id,userId, title, content, time, type, evaluate FROM feedback WHERE response IS NULL")
    List<Feedback> getFeedbackList();

    //每一个类型的数量
    @Select("SELECT type, COUNT(*) AS count FROM feedback WHERE response IS NULL GROUP BY type")
    List<Map<String, Integer>> getFeedbackCountByType();

    //获取某个人的反馈信息
    @Select("SELECT * FROM feedback WHERE userId = #{userId}")
    List<Feedback> getFeedbackByUserId(int userId);

    //解决反馈
    @Update("UPDATE feedback SET response=#{response} WHERE id = #{id}")
    int solveFeedback(int id,String response);
}
