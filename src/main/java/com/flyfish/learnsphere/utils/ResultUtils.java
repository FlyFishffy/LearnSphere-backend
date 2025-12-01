package com.flyfish.learnsphere.utils;


import com.flyfish.learnsphere.model.entity.Result;
import com.flyfish.learnsphere.model.enums.ErrorCode;

/**
 * 返回工具类
 * @Author: FlyFish
 * @CreateTime: 2025/11/19
 */
public class ResultUtils {
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success(int code, T data) {
        return new Result<>(code, data);
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode);
    }

    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
