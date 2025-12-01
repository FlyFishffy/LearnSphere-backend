package com.flyfish.learnsphere.controller;


import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.model.dto.UserLoginRequest;
import com.flyfish.learnsphere.model.dto.UserRegisterRequest;
import com.flyfish.learnsphere.model.entity.Result;
import com.flyfish.learnsphere.model.entity.User;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.enums.RoleType;
import com.flyfish.learnsphere.model.vo.LoginUserVO;
import com.flyfish.learnsphere.service.UserService;
import com.flyfish.learnsphere.utils.ResultUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/19
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if(userAccount == null || password == null || checkPassword == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userId = userService.userRegister(userAccount, password, checkPassword);
        return ResultUtils.success(userId);
    }


    /**
     * 用户登录
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/login")
    public Result<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if(userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getPassword();
        if(userAccount == null || password == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO userVO = userService.userLogin(userAccount, password, request);
        return ResultUtils.success(userVO);
    }


    /**
     * 用户退出登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Result<Boolean> userLogout(HttpServletRequest request) {
        if(request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    /**
     * 获取已登录用户信息
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public Result<LoginUserVO> getLoginUser(HttpServletRequest request) {
        if(request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        loginUserVO.setRoleType(RoleType.getByValue(user.getRoleType()).getDescription());
        return ResultUtils.success(loginUserVO);
    }
}
