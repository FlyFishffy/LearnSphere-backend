package com.flyfish.learnsphere.model.dto;


import lombok.Data;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/20
 */
@Data
public class UserLoginRequest {

    private String userAccount;

    private String password;
}
