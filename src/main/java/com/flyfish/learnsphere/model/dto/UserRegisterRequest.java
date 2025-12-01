package com.flyfish.learnsphere.model.dto;


import lombok.Data;

/**
 * 用户注册请求
 * @Author: FlyFish
 * @CreateTime: 2025/11/19
 */
@Data
public class UserRegisterRequest {

    private String userAccount;

    private String password;

    private String checkPassword;
}
