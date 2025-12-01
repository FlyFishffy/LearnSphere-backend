package com.flyfish.learnsphere.handler;


import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.model.entity.Result;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.utils.ResultUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局统一异常处理
 * @Author: FlyFish
 * @CreateTime: 2025/11/23
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

//    @ExceptionHandler(RuntimeException.class)
//    public Result<?> runtimeExceptionHandler(RuntimeException e) {
//        return ResultUtils.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统内部错误");
//    }
}
