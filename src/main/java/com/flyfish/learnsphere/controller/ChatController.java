package com.flyfish.learnsphere.controller;


import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.model.dto.ChatRequest;
import com.flyfish.learnsphere.model.entity.Result;
import com.flyfish.learnsphere.model.entity.User;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.vo.MessageVO;
import com.flyfish.learnsphere.service.ChatService;
import com.flyfish.learnsphere.service.UserService;
import com.flyfish.learnsphere.utils.ResultUtils;
import dev.langchain4j.data.message.ChatMessage;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/26
 */
@RestController
@RequestMapping("/ai")
@Slf4j
public class ChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private UserService userService;

    /**
     * 用户提问
     * @param chatRequest
     * @return
     */
    // todo 改成流式响应
    @PostMapping("/ask")
    public SseEmitter ask(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if(chatRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return chatService.ask(chatRequest, user.getId());
    }


    /**
     * 获取用户对话历史
     * @param request
     * @return
     */
    @GetMapping("/get/session")
    public Result<List<String>> getSessions(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<String> sessions = chatService.getSessions(user.getId());
        return ResultUtils.success(sessions);
    }

    @GetMapping("/get/session/{sessionId}")
    public Result<List<MessageVO>> getSessionHistory(@PathVariable String sessionId, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<MessageVO> sessionHistory = chatService.getSessionHistory(user.getId(), sessionId);
        return ResultUtils.success(sessionHistory);
    }
}
