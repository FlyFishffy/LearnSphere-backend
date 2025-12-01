package com.flyfish.learnsphere.model.entity;


import com.flyfish.learnsphere.model.enums.ErrorCode;
import lombok.Data;

/**
 * 前端统一返回类
 * @Author: FlyFish
 * @CreateTime: 2025/11/19
 */
@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public Result(int code, String msg, T data) {
        this.code = code;
        this.message = msg;
        this.data = data;
    }

    public Result(int code, T data) {
        this.code = code;
        this.data = data;
        this.message = "";
    }

    public Result(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.data = null;
    }
}
