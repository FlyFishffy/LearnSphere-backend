package com.flyfish.learnsphere.model.vo;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/20
 */
@Data
public class LoginUserVO {

    private Long id;

    private String userName;

    private String roleType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
