package com.example.backend.mapper;

import com.example.backend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    //添加user
    @Insert("INSERT INTO user(account,password,username,email,avatar) VALUES(#{account},#{password},#{username},#{email},#{avatar})")
    int insertUser(User user);

    //登陆
    @Select("SELECT * FROM user WHERE account = #{account} AND password = #{password}")
    User login(String account, String password);

    //检查账号是否存在
    @Select("SELECT COUNT(*) FROM user WHERE account = #{account}")
    int checkAccountExists(String account);

    //通过id获取用户的消息
    @Select("SELECT * FROM user WHERE id = #{id}")
    User getUserById(int id);
}
