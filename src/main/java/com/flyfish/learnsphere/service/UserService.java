package com.flyfish.learnsphere.service;


import com.flyfish.learnsphere.model.entity.User;
import com.flyfish.learnsphere.model.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/19
 */
public interface UserService {
    /**
     * 用户注册
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return              新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 用户登录
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param request       用于设置session
     * @return              LoginUserVO
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 用户退出登录
     * @param request   用于获取用户session
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 获取已登录用户
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);
}
