package com.txy.chefdemo.controller;

import com.txy.chefdemo.aspect.LogExecution;
import com.txy.chefdemo.constant.AuthConstant;
import com.txy.chefdemo.req.*;
import com.txy.chefdemo.resp.LogInResp;
import com.txy.chefdemo.resp.RegisterResp;
import com.txy.chefdemo.resp.SimpleResp;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.AuthOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthOperationService authOperationService;

    /**
     * 注册账号：
     * 1. 校验用户名、密码和角色是否传入；
     * 2. 校验用户名是否已存在；
     * 3. 对密码做 SHA-256 加密后创建用户；
     * 4. 若注册角色是普通用户，则同步初始化钱包。
     */
    @LogExecution(returnType = RegisterResp.class)
    @PostMapping("/register")
    public RegisterResp register(@RequestBody RegisterReq registerReq) {
        return authOperationService.register(registerReq);
    }

    /**
     * 登录：
     * 1. 校验用户名、密码、角色；
     * 2. 查询用户并校验角色、账号状态和密码；
     * 3. 更新最近登录时间；
     * 4. 生成 JWT，并把 token 存入 Redis 作为当前登录态。
     */
    @LogExecution(returnType =  LogInResp.class)
    @PostMapping("/login")
    public LogInResp login(@RequestBody LogInReq logInReq) {
        return authOperationService.login(logInReq);
    }

    /**
     * 退出登录：
     * 1. 从请求头读取当前 token；
     * 2. 解析当前登录用户；
     * 3. 删除 Redis 中保存的登录 token，使当前登录态失效。
     */
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/logout")
    public SimpleResp logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthConstant.REQUEST_USER_ID);
        authOperationService.logout(userId, request.getHeader(AuthConstant.AUTHORIZATION_HEADER));
        return new SimpleResp(BaseRespConstant.SUC);
    }
}
