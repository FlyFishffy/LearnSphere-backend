package com.flyfish.learnsphere.model.enums;


/**
 * 课程分类枚举
 * @Author: FlyFish
 * @CreateTime: 2025/11/23
 */
public enum CourseCategory {

    COMPUTER_SCIENCE(1, "COMPUTER_SCIENCE"),
    ARTIFICIAL_INTELLIGENCE(2, "ARTIFICIAL_INTELLIGENCE"),
    CYBER_SECURITY(3, "CYBER_SECURITY"),
    SOFTWARE_ENGINEERING(4, "SOFTWARE_ENGINEERING"),
    ELECTRONIC_ENGINEERING(5, "ELECTRONIC_ENGINEERING"),
    MATH_AND_FOUNDATION(6, "MATH_AND_FOUNDATION");

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
