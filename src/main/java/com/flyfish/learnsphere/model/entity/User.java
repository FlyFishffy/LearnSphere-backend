package com.flyfish.learnsphere.model.entity;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/20
 */
@Data
public class User {
    Long id;

    String userAccount;

    String password;

    String userName;

    String email;

    String phone;

    Integer roleType;

    Integer status;

    LocalDateTime lastLoginTime;

    LocalDateTime createTime;

    LocalDateTime updateTime;
}
