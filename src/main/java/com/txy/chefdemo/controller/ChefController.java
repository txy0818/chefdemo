package com.txy.chefdemo.controller;

import com.txy.chefdemo.aspect.LogExecution;
import com.txy.chefdemo.domain.constant.UserRole;
import com.txy.chefdemo.domain.dto.ChefProfileDTO;
import com.txy.chefdemo.domain.dto.OrderViewDTO;
import com.txy.chefdemo.req.*;
import com.txy.chefdemo.resp.*;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.ChefOperationService;
import com.txy.chefdemo.utils.AuthRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/chef")
public class ChefController {

    @Autowired
    private ChefOperationService chefOperationService;

    // 厨师模块页面：
    // page1: 个人资料管理页（查看/编辑厨师资料、上传证件、提交审核）
    // page2: 预约时间段管理页（新增/删除可预约时间段、查看时间段列表）
    // page3: 订单管理页（查看订单列表、接单/拒单/完成订单）
    // page4: 个人中心页（查看/编辑个人资料、修改密码）写到 AuthController 中了

    /**
     * 查询厨师资料：
     * 1. 按用户 ID 查询厨师资料；
     * 2. 若不存在则直接报错；
     * 3. 将资料、证件、审核状态、评分等转换成前端展示 DTO。
     */
    @LogExecution(returnType = DataResp.class)
    @PostMapping("/profile/get")
    public DataResp<ChefProfileDTO> getProfile(HttpServletRequest request) {
        Long currentChefId = AuthRequestUtils.requireUser(request, UserRole.CHEF);
        return new DataResp<>(BaseRespConstant.SUC, chefOperationService.getProfile(currentChefId));
    }

    /**
     * 保存厨师资料：
     * 1. 校验用户存在；
     * 2. 首次保存时新增厨师资料，二次保存时更新资料；
     * 3. 若和当前资料完全一致则拦截重复提交；
     * 4. 保存后写入一条待审核记录，等待管理员审核。
     */
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/profile/save")
    public SimpleResp saveProfile(@RequestBody SaveChefProfileReq req, HttpServletRequest request) {
        Long currentChefId = AuthRequestUtils.requireUser(request, UserRole.CHEF);
        chefOperationService.saveProfile(currentChefId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }


    /**
     * 新增可预约时间段：
     * 1. 校验厨师已审核通过；
     * 2. 校验开始结束时间合法；
     * 3. 检查与现有可用时间段是否重叠；
     * 4. 新增一条可预约时间段记录。
     */
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/time/add")
    public SimpleResp addAvailableTime(@RequestBody AddAvailableTimeReq req, HttpServletRequest request) {
        Long currentChefId = AuthRequestUtils.requireUser(request, UserRole.CHEF);
        chefOperationService.addAvailableTime(currentChefId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }

    /**
     * 分页查询厨师时间段列表：
     * 1. 校验厨师已审核通过；
     * 2. 根据厨师 ID 和分页参数查询时间段；
     * 3. 返回时间段状态、开始结束时间和总数。
     */
    @LogExecution(returnType = ListAvailableTimesResp.class)
    @PostMapping("/time/list")
    public ListAvailableTimesResp listAvailableTimes(@RequestBody ListAvailableTimesReq req, HttpServletRequest request) {
        Long currentChefId = AuthRequestUtils.requireUser(request, UserRole.CHEF);
        return chefOperationService.listAvailableTimes(currentChefId, req);
    }

    /**
     * 删除时间段：
     * 1. 校验厨师已审核通过且时间段属于当前厨师；
     * 2. 查询该时间段是否已被有效订单占用；
     * 3. 未占用则将状态更新为已删除。
     */
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/time/delete")
    public SimpleResp deleteAvailableTime(@RequestBody DeleteAvailableTimeReq req, HttpServletRequest request) {
        Long currentChefId = AuthRequestUtils.requireUser(request, UserRole.CHEF);
        chefOperationService.deleteAvailableTime(currentChefId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }

    /**
     * 查询厨师侧订单详情：
     * 1. 校验订单存在；
     * 2. 校验当前厨师对该订单有查看权限；
     * 3. 组装订单、用户、支付状态和所属时间段信息返回前端。
     */
    @LogExecution(returnType = DataResp.class)
    @PostMapping("/order/detail")
    public DataResp<OrderViewDTO> orderDetail(@RequestBody QueryOrderDetailReq req, HttpServletRequest request) {
        Long currentChefId = AuthRequestUtils.requireUser(request, UserRole.CHEF);
        return new DataResp<>(BaseRespConstant.SUC, chefOperationService.orderDetail(currentChefId, req));
    }

    /**
     * 分页查询厨师订单列表：
     * 1. 校验厨师已审核通过；
     * 2. 根据状态和分页条件查询厨师名下订单；
     * 3. 返回订单基础信息、支付状态和所属时间段。
     */
    @LogExecution(returnType = QueryChefOrderResp.class)
    @PostMapping("/order/list")
    public QueryChefOrderResp orderList(@RequestBody QueryChefOrderReq req, HttpServletRequest request) {
        Long currentChefId = AuthRequestUtils.requireUser(request, UserRole.CHEF);
        return chefOperationService.orderList(currentChefId, req);
    }

    /**
     * 厨师接单：
     * 1. 校验订单属于当前厨师；
     * 2. 仅允许待接单状态接单；
     * 3. 通过状态机流转为已接单，并触发通知。
     */
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/order/accept")
    public SimpleResp acceptOrder(@RequestBody ChefOrderActionReq req, HttpServletRequest request) {
        Long currentChefId = AuthRequestUtils.requireUser(request, UserRole.CHEF);
        chefOperationService.acceptOrder(currentChefId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }


    /**
     * 厨师完成订单：
     * 1. 校验订单属于当前厨师；
     * 2. 仅允许已接单状态操作；
     * 3. 必须已到预约结束时间后才能完成；
     * 4. 通过状态机流转为已完成。
     */
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/order/complete")
    public SimpleResp completeOrder(@RequestBody ChefOrderActionReq req, HttpServletRequest request) {
        Long currentChefId = AuthRequestUtils.requireUser(request, UserRole.CHEF);
        chefOperationService.completeOrder(currentChefId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }

    /**
     * 厨师拒单：
     * 1. 校验拒单原因和订单归属；
     * 2. 仅允许待接单状态拒单；
     * 3. 通过状态机流转为已拒单；
     * 4. 若订单已支付，则同步退款并更新支付状态。
     */
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/order/reject")
    public SimpleResp rejectOrder(@RequestBody ChefOrderActionReq req, HttpServletRequest request) {
        Long currentChefId = AuthRequestUtils.requireUser(request, UserRole.CHEF);
        chefOperationService.rejectOrder(currentChefId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }
}
