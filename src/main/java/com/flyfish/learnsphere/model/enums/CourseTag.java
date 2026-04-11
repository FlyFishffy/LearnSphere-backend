package com.flyfish.learnsphere.model.enums;


/**
 * 课程标签枚举
 * @Author: FlyFish
 * @CreateTime: 2025/11/23
 */
public enum CourseTag {

    DATA_STRUCTURE(1, "数据结构与算法"),
    OPERATING_SYSTEM(2, "操作系统"),
    COMPUTER_NETWORK(3, "计算机网络"),
    COMPILER(4, "编译原理"),
    DATABASE(5, "数据库系统"),
    MACHINE_LEARNING(6, "机器学习"),
    INFORMATION_SECURITY(7, "信息安全"),
    COMPUTER_ARCHITECTURE(8, "计算机体系结构"),
    PROGRAMMING(9, "程序设计"),
    SIGNAL_PROCESSING(10, "信号处理");

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
