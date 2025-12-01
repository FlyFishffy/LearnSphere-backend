package com.flyfish.learnsphere.service.impl;


import com.flyfish.learnsphere.constant.CommonConstant;
import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.mapper.UserMapper;
import com.flyfish.learnsphere.model.entity.User;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.enums.RoleType;
import com.flyfish.learnsphere.model.vo.LoginUserVO;
import com.flyfish.learnsphere.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/20
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        if(userAccount == null || userPassword == null || checkPassword == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户名过短");
        }
        if(userPassword.length() < 8 || userPassword.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不符合要求");
        }
        if(!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }

        synchronized (userAccount.intern()) {
            if(userMapper.isUserExist(userAccount)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "账号重复");
            }
            User user = new User();
            user.setUserAccount(userAccount);
            String encryptPassword = passwordEncoder.encode(userPassword);
            user.setPassword(encryptPassword);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setRoleType(RoleType.STUDENT.getValue());
            user.setUserName(userAccount);
            user.setStatus(1);
            return userMapper.save(user);
        }
    }


    /**
     * 用户登录
     * @param userAccount   用户账户
     * @param password      用户密码
     * @param request       用于设置session
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String password, HttpServletRequest request) {
        if(userAccount == null || password == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        User user = userMapper.getUserByAccount(userAccount);
        if(user == null) {
            log.warn("User login failed. User account is not exist.");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "User account is not exist.");
        }
        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "User password is not match.");
        }
        request.getSession().setAttribute(CommonConstant.USER_LOGIN, user);
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateUser(user);
        loginUserVO.setRoleType(RoleType.getByValue(user.getRoleType()).getDescription());
        return loginUserVO;
    }


    /**
     * 用户退出登录
     * @param request   用于获取用户session
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if(request.getSession().getAttribute(CommonConstant.USER_LOGIN) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        request.getSession().removeAttribute(CommonConstant.USER_LOGIN);
        return true;
    }


    /**
     * 获取已登录用户
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        User curUser = (User) request.getSession().getAttribute(CommonConstant.USER_LOGIN);
        if(curUser == null || curUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = curUser.getId();
        curUser = userMapper.getUserById(userId);
        if(curUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return curUser;
    }
}
