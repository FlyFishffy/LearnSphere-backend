package com.flyfish.learnsphere.aop;


import com.flyfish.learnsphere.annotaion.AuthCheck;
import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.model.entity.User;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.enums.RoleType;
import com.flyfish.learnsphere.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.Set;

/**
 * 权限校验 AOP
 * @Author: FlyFish
 * @CreateTime: 2025/11/21
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doAuthCheck(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String[] allowedRoles = authCheck.value();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        if(allowedRoles == null || allowedRoles.length == 0){
            return joinPoint.proceed();
        }
        RoleType roleType = RoleType.getByValue(loginUser.getRoleType());
        if(roleType == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Set<RoleType> set = new HashSet<>();
        for(String allowedRole : allowedRoles) {
            set.add(RoleType.getByDescription(allowedRole));
        }
        if(!set.contains(roleType)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
