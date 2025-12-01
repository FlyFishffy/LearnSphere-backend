package com.flyfish.learnsphere.model.enums;


/**
 * 课程标签枚举
 * @Author: FlyFish
 * @CreateTime: 2025/11/23
 */
public enum CourseTag {

    AI(1, "AI"),
    JAVA(2, "Java"),
    C_PLUS_PLUS(3, "C++"),
    GOLANG(4, "Golang");

    private Integer value;

    private String description;

    CourseTag(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public Integer getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static CourseTag getByValue(Integer value){
        if(value == null){
            return null;
        }
        for(CourseTag courseTag : CourseTag.values()){
            if(courseTag.getValue().equals(value)){
                return courseTag;
            }
        }
        return null;
    }
}
