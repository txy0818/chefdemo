package com.txy.chefdemo.controller;

import com.txy.chefdemo.aspect.LogExecution;
import com.txy.chefdemo.req.UpdatePasswordReq;
import com.txy.chefdemo.req.UpdateProfileReq;
import com.txy.chefdemo.resp.QueryInfoResp;
import com.txy.chefdemo.resp.SimpleResp;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.utils.AuthRequestUtils;
import com.txy.chefdemo.service.ProfileOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileOperationService profileOperationService;

    /**
     * 查询个人中心基础资料：
     * 1. 校验 userId；
     * 2. 查询用户信息；
     * 3. 组装用户名、头像、手机号、角色、状态、最近登录时间返回前端。
     */
    @LogExecution(returnType = QueryInfoResp.class)
    @PostMapping("/queryInfo")
    public QueryInfoResp queryInfo(HttpServletRequest request) {
        Long userId = AuthRequestUtils.requireCurrentUserId(request);
        return new QueryInfoResp(BaseRespConstant.SUC, profileOperationService.queryInfo(userId));
    }

    /**
     * 更新个人中心资料：
     * 1. 校验用户是否存在；
     * 2. 判断头像/手机号是否真的有变化；
     * 3. 更新用户表中的头像和手机号。
     */
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/updateUserInfo")
    public SimpleResp updateUserInfo(@RequestBody UpdateProfileReq req, HttpServletRequest request) {
        Long userId = AuthRequestUtils.requireCurrentUserId(request);
        profileOperationService.updateUserInfo(userId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }

    /**
     * 修改密码：
     * 1. 校验用户、旧密码、新密码；
     * 2. 比对旧密码是否正确；
     * 3. 禁止新旧密码相同；
     * 4. 更新密码后清理 Redis 登录态，要求重新登录。
     */
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/updatePassword")
    public SimpleResp updatePassword(@RequestBody UpdatePasswordReq req, HttpServletRequest request) {
        Long userId = AuthRequestUtils.requireCurrentUserId(request);
        profileOperationService.updatePassword(userId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }
}
