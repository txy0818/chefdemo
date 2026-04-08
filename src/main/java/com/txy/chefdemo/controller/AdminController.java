package com.txy.chefdemo.controller;

import com.txy.chefdemo.aspect.LogExecution;
import com.txy.chefdemo.domain.constant.UserRole;
import com.txy.chefdemo.domain.dto.OrderStatisticsDTO;
import com.txy.chefdemo.req.AuditChefReq;
import com.txy.chefdemo.req.AuditReviewReq;
import com.txy.chefdemo.req.DeleteReviewReq;
import com.txy.chefdemo.req.HandleReportReq;
import com.txy.chefdemo.req.QueryAuditChefReq;
import com.txy.chefdemo.req.QueryChefReq;
import com.txy.chefdemo.req.QueryReportReq;
import com.txy.chefdemo.req.QueryReviewReq;
import com.txy.chefdemo.req.QueryUserListReq;
import com.txy.chefdemo.req.QueryUserOrderReq;
import com.txy.chefdemo.req.UpdateUserStatusReq;
import com.txy.chefdemo.resp.DataResp;
import com.txy.chefdemo.resp.QueryAuditChefResp;
import com.txy.chefdemo.resp.QueryChefResp;
import com.txy.chefdemo.resp.QueryReportResp;
import com.txy.chefdemo.resp.QueryReviewResp;
import com.txy.chefdemo.resp.QueryUserListResp;
import com.txy.chefdemo.resp.QueryUserOrderResp;
import com.txy.chefdemo.resp.SimpleResp;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.AdminOperationService;
import com.txy.chefdemo.service.AdminQueryService;
import com.txy.chefdemo.service.AdminReviewReportService;
import com.txy.chefdemo.utils.AuthRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminOperationService adminOperationService;
    @Autowired
    private AdminReviewReportService adminReviewReportService;
    @Autowired
    private AdminQueryService adminQueryService;

    /**
     * 说明：
     * 1. 当前登录管理员的 userId 由 token 解析后放在 request attribute 中；
     * 2. 请求体里的 userId 仍然是前端传入的查询条件，用于筛选目标用户/厨师。
     *
     * 查询厨师资料列表：
     * 1. 根据审核状态、用户 ID、用户名和分页参数构建条件；
     * 2. 查询厨师资料列表；
     * 3. 转换为厨师资料 DTO 返回前端。
     */
    @LogExecution(returnType = QueryChefResp.class)
    @PostMapping("/chef/list")
    public QueryChefResp queryChefList(@RequestBody QueryChefReq req, HttpServletRequest request) {
        AuthRequestUtils.requireUser(request, UserRole.ADMIN);
        return adminQueryService.queryChefList(req);
    }

    /**
     * 查询用户/厨师列表：
     * 1. 根据角色、状态、用户 ID、用户名和分页参数构建条件；
     * 2. 查询用户列表；
     * 3. 转换为用户 DTO 并返回总数。
     */
    @LogExecution(returnType = QueryUserListResp.class)
    @PostMapping("/user/list")
    public QueryUserListResp queryUserList(@RequestBody QueryUserListReq req, HttpServletRequest request) {
        AuthRequestUtils.requireUser(request, UserRole.ADMIN);
        return adminQueryService.queryUserList(req);
    }

    /**
     * 更新用户账号状态：
     * 1. 校验用户存在和处理原因；
     * 2. 更新用户状态；
     * 3. 删除 Redis 登录态并记录状态变更日志；
     * 4. 发送通知；
     * 5. 若冻结的是厨师，则发布事件处理相关订单(applicationEvent) 订单退单/待审核评论驳回/待审核举报驳回。
     */
    @Transactional
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/user/status/updateUserStatus")
    public SimpleResp updateUserStatus(@RequestBody UpdateUserStatusReq req, HttpServletRequest request) {
        Long currentAdminId = AuthRequestUtils.requireUser(request, UserRole.ADMIN);
        adminOperationService.updateUserStatus(currentAdminId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }

    /**
     * 查询待审核厨师列表：
     * 1. 查询厨师最新待审核记录；
     * 2. 只返回账号状态正常的厨师资料；
     * 3. 分页返回前端展示。
     */
    @Transactional
    @LogExecution(returnType = QueryAuditChefResp.class)
    @PostMapping("/chef/queryAuditChef")
    public QueryAuditChefResp queryAuditChef(@RequestBody QueryAuditChefReq req, HttpServletRequest request) {
        AuthRequestUtils.requireUser(request, UserRole.ADMIN);
        return adminQueryService.queryAuditChef(req);
    }

    /**
     * 审核厨师资料：
     * 1. 校验厨师资料和待审核记录存在；
     * 2. 更新审核记录和厨师资料审核状态；
     * 3. 若驳回则记录驳回原因；
     * 4. 审核结果通过通知发送给厨师。
     */
    @Transactional
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/chef/audit")
    public SimpleResp auditChef(@RequestBody AuditChefReq req, HttpServletRequest request) {
        Long currentAdminId = AuthRequestUtils.requireUser(request, UserRole.ADMIN);
        adminOperationService.auditChef(currentAdminId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }

    /**
     * 查询评论列表：
     * 1. 根据审核状态和分页条件查询评论；
     * 2. 只查询正常状态的评论；
     * 3. 转换为评论 DTO 返回前端。
     */
    @LogExecution(returnType = QueryReviewResp.class)
    @PostMapping("/review/reviewList")
    public QueryReviewResp reviewList(@RequestBody QueryReviewReq req, HttpServletRequest request) {
        AuthRequestUtils.requireUser(request, UserRole.ADMIN);
        return adminReviewReportService.reviewList(req);
    }

    /**
     * 审核评论：
     * 1. 校验审核状态和驳回原因；
     * 2. 根据订单找到评论；
     * 3. 更新评论审核状态和审核原因。
     */
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/review/audit")
    public SimpleResp auditReview(@RequestBody AuditReviewReq req, HttpServletRequest request) {
        AuthRequestUtils.requireUser(request, UserRole.ADMIN);
        adminReviewReportService.auditReview(req);
        return new SimpleResp(BaseRespConstant.SUC);
    }

    /**
     * 删除评论：
     * 1. 根据订单找到评论；
     * 2. 将评论状态更新为已删除，实现逻辑删除。
     */
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/review/delete")
    public SimpleResp deleteReview(@RequestBody DeleteReviewReq req, HttpServletRequest request) {
        AuthRequestUtils.requireUser(request, UserRole.ADMIN);
        adminReviewReportService.deleteReview(req);
        return new SimpleResp(BaseRespConstant.SUC);
    }

    /**
     * 查询举报列表：
     * 1. 根据举报状态和分页参数查询举报；
     * 2. 转换举报 DTO；
     * 3. 返回举报列表和总数。
     */
    @LogExecution(returnType = QueryReportResp.class)
    @PostMapping("/report/reportList")
    public QueryReportResp reportList(@RequestBody QueryReportReq req, HttpServletRequest request) {
        AuthRequestUtils.requireUser(request, UserRole.ADMIN);
        return adminReviewReportService.reportList(req);
    }

    /**
     * 处理举报：
     * 1. 校验举报存在和处理结果；
     * 2. 更新举报状态、处理结果和处理人；
     * 3. 保存处理时间。
     */
    @Transactional
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/report/handle")
    public SimpleResp handleReport(@RequestBody HandleReportReq req, HttpServletRequest request) {
        Long currentAdminId = AuthRequestUtils.requireUser(request, UserRole.ADMIN);
        adminReviewReportService.handleReport(currentAdminId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }

    /**
     * 查询全平台订单列表：
     * 1. 根据订单状态和分页条件查询订单；
     * 2. 组装订单 DTO；
     * 3. 返回订单列表和总数。
     */
    @LogExecution(returnType = QueryUserOrderResp.class)
    @PostMapping("/order/list")
    public QueryUserOrderResp orderList(@RequestBody QueryUserOrderReq req, HttpServletRequest request) {
        AuthRequestUtils.requireUser(request, UserRole.ADMIN);
        return adminQueryService.orderList(req);
    }

    /**
     * 订单统计：
     * 1. 查询最近一周订单；
     * 2. 统计各订单状态数量；
     * 3. 生成今日饼图数据和近一周趋势数据；
     * 4. 返回给前端做图表展示。
     */
    @LogExecution(returnType = DataResp.class)
    @PostMapping("/statistics/order")
    public DataResp<OrderStatisticsDTO> orderStatistics(HttpServletRequest request) {
        AuthRequestUtils.requireUser(request, UserRole.ADMIN);
        return new DataResp<>(BaseRespConstant.SUC, adminQueryService.orderStatistics());
    }
}
