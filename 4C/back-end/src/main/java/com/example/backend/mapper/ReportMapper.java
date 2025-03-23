package com.example.backend.mapper;

import com.example.backend.entity.Report;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ReportMapper {
    //插入报告
    @Insert("INSERT INTO report(userId,filename,time,starttime,endtime,data,analyse,type,isshow,password) VALUES (#{userId},#{filename},#{time},#{starttime},#{endtime},#{data},#{analyse},#{type},#{isshow},#{password})")
    int insertReport(Report report);

    //获取所有可以查看的报告
    @Select("SELECT * FROM report WHERE userId=#{userId} AND isshow='true'")
    List<Report> getAllReportAndShow(Integer userId);

    //获取所有报告
    @Select("SELECT * FROM report WHERE userId=#{userId}")
    List<Report> getAllReport(Integer userId);

    //通过id获取报告
    @Select("SELECT * FROM report WHERE id=#{id}")
    Report getReportById(Integer id);

    //是否展示报告
    @Update("UPDATE report SET isshow=#{isshow} WHERE id=#{id}")
    int updateReportShow(Integer id, String isshow);

    //获取最上面的报告
    @Select("SELECT * FROM report WHERE userId = #{userId} ORDER BY id DESC LIMIT 1")
    Report getLastReport(Integer userId);

    //总共的报告数量
    @Select("SELECT COUNT(*) FROM report WHERE userId = #{userId}")
    int getTotalReportCount(Integer userId);

    //更新密码
    @Update("UPDATE report SET password = #{password} WHERE id = #{id}")
    int updatePassword(Integer id, String password);

    //通过id和password获取报告
    @Select("SELECT * FROM report WHERE id = #{id} AND password = #{password}")
    Report getReportByIdAndPassword(Integer id, String password);
}
