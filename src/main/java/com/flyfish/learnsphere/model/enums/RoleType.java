package com.flyfish.learnsphere.model.enums;


import org.apache.commons.lang3.ObjectUtils;

/**
 * 用户角色分类
 * @Author: FlyFish
 * @CreateTime: 2025/11/20
 */
public enum RoleType {

    STUDENT(1, "Student"),
    TEACHER(2, "Teacher"),
    ADMIN(3, "Admin");

    private final int value;

    private final String description;

    RoleType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static RoleType getByValue(Integer value) {
        if(ObjectUtils.isEmpty(value)){
            return null;
        }
        for (RoleType roleType : RoleType.values()) {
            if (roleType.getValue() == value) {
                return roleType;
            }
        }
        return null;
    }

    public static RoleType getByDescription(String description) {
        if(ObjectUtils.isEmpty(description)){
            return null;
        }
        for (RoleType roleType : RoleType.values()) {
            if (roleType.getDescription().equals(description)) {
                return roleType;
            }
        }
        return null;
    }
}
