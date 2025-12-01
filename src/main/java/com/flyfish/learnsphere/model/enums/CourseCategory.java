package com.flyfish.learnsphere.model.enums;


/**
 * 课程分类枚举
 * @Author: FlyFish
 * @CreateTime: 2025/11/23
 */
public enum CourseCategory {

    AI(1, "AI"),
    BACKEND(2, "BACKEND"),
    FRONTEND(3, "FRONTEND"),
    COMPUTER_NET(4, "COMPUTER_NET");

    private final Integer value;

    private final String description;

    CourseCategory(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public Integer getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static CourseCategory getByValue(Integer value){
        if(value == null){
            return null;
        }
        for(CourseCategory courseCategory : CourseCategory.values()){
            if(value.equals(courseCategory.value)){
                return courseCategory;
            }
        }
        return null;
    }
}
