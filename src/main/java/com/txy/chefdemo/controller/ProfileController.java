package com.txy.chefdemo.controller;

import com.txy.chefdemo.aspect.LogExecution;
import com.txy.chefdemo.domain.NotificationRecord;
import com.txy.chefdemo.req.QueryNotificationReq;
import com.txy.chefdemo.req.ReadNotificationReq;
import com.txy.chefdemo.req.UpdatePasswordReq;
import com.txy.chefdemo.req.UpdateProfileReq;
import com.txy.chefdemo.resp.ListResp;
import com.txy.chefdemo.resp.QueryInfoResp;
import com.txy.chefdemo.resp.SimpleResp;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.ProfileOperationService;
import com.txy.chefdemo.service.UserInteractionService;
import com.txy.chefdemo.utils.AuthRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
    @Autowired
    private UserInteractionService userInteractionService;

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

    /**
     * 分页查询通知列表：
     * 1. 根据当前登录用户 ID 查询通知；
     * 2. 支持只看未读和分页查询；
     * 3. 返回通知列表和总数。
     */
    @LogExecution(returnType = ListResp.class)
    @PostMapping("/notification/list")
    public ListResp<NotificationRecord> notificationList(@RequestBody QueryNotificationReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireCurrentUserId(request);
        return userInteractionService.notificationList(currentUserId, req);
    }

    /**
     * 标记通知已读：
     * 1. 校验通知存在且属于当前登录用户；
     * 2. 将通知状态更新为已读。
     */
    @Transactional
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/notification/read")
    public SimpleResp readNotification(@RequestBody ReadNotificationReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireCurrentUserId(request);
        userInteractionService.readNotification(currentUserId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }
}
