package com.flyfish.learnsphere.mapper;


import com.flyfish.learnsphere.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/20
 */
@Mapper
public interface UserMapper {

    /**
     * 根据账户名判断用户是否存在
     * @param userAccount
     * @return
     */
    boolean isUserExist(String userAccount);


    /**
     * 保存用户
     * @param user
     * @return 用户id
     */
    long save(User user);


    /**
     * 根据账户名和密码获取用户
     * @param userAccount
     * @return
     */
    User getUserByAccount(String userAccount);


    /**
     * 根据ID获取用户
     * @param userId
     * @return
     */
    User getUserById(Long userId);


    /**
     * 更新用户信息
     * @param user
     */
    void updateUser(User user);
}
