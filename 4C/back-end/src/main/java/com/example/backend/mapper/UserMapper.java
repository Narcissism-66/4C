package com.example.backend.mapper;

import com.example.backend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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

    //更新用户信息
    @Update("UPDATE user SET username = #{username}, avatar = #{avatar}, email = #{email} WHERE id = #{id}")
    int updateUser(User user);

    //更新使用时间
    @Update("UPDATE user SET usageTime = usageTime + #{usageTime} WHERE id = #{id}")
    int updateUsageTime(Integer id,Integer usageTime);

    //获取每个用户的usageTime和其它信息
    @Select("SELECT * FROM user ORDER BY usageTime DESC LIMIT 5")
    List<User> getAllUsers();

    //获取所有的用户信息
    @Select("SELECT * FROM user")
    List<User> getAllUsersInfo();

    //更新用户的身份
    @Update("UPDATE user SET role = #{role} WHERE id = #{id}")
    int updateUserRole(Integer id, String role);

    //删除用户
    @Update("DELETE FROM user WHERE id = #{id}")
    int deleteUser(Integer id);

    //通过用户名搜索用户
    @Select("SELECT * FROM user WHERE username LIKE CONCAT('%', #{username}, '%')")
    List<User> searchUsersByName(String username);

    //更新bg
    @Update("UPDATE user SET bg = #{bg} WHERE id = #{id}")
    int updateUserBg(Integer id, String bg);

    //更新bgcolor
    @Update("UPDATE user SET bgColor = #{bgColor} WHERE id = #{id}")
    int updateUserBgColor(Integer id, String bgColor);
}
