package com.txy.chefdemo.controller;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.aspect.LogExecution;
import com.txy.chefdemo.domain.NotificationRecord;
import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.bo.ReservationOrderSearchBo;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.domain.constant.PayStatus;
import com.txy.chefdemo.domain.constant.UserRole;
import com.txy.chefdemo.domain.dto.ChefDetailDTO;
import com.txy.chefdemo.domain.dto.OrderViewDTO;
import com.txy.chefdemo.domain.dto.UserWalletDTO;
import com.txy.chefdemo.exp.BusinessException;
import com.txy.chefdemo.req.*;
import com.txy.chefdemo.resp.*;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.OrderFlowService;
import com.txy.chefdemo.service.ReservationOrderService;
import com.txy.chefdemo.service.UserInteractionService;
import com.txy.chefdemo.service.UserOrderService;
import com.txy.chefdemo.service.UserQueryService;
import com.txy.chefdemo.transition.order.OrderContext;
import com.txy.chefdemo.transition.order.OrderStateEvent;
import com.txy.chefdemo.utils.AuthRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private ReservationOrderService reservationOrderService;
    @Autowired
    private OrderFlowService orderFlowService;
    @Autowired
    private UserOrderService userOrderService;
    @Autowired
    private UserInteractionService userInteractionService;
    @Autowired
    private UserQueryService userQueryService;
    @Resource
    private RedissonClient redissonClient;

    private volatile boolean running = true;
    private Thread cancelOrderThread;

    // 用户模块页面：
    // page1: 厨师搜索页（搜索/筛选厨师列表：按价格、评分、服务区域、关键词）
    // page2: 厨师详情页（查看厨师完整资料、评价列表、可预约时间段）
    // page3: 创建订单页（选择时间段、填写预约信息、确认订单）
    // page4: 订单列表页（查看我的订单：待支付/待接单/已接单/已完成/已取消）
    // page5: 订单详情页（查看订单详情、支付/取消订单）
    // page6: 评价页（对已完成订单进行评分和评价）
    // page7: 举报页（对已完成订单的厨师进行举报）
    // page8: 个人中心页（查看/编辑个人资料、修改密码）统一到 ProfileController 中
    // page9: 通知列表页（查看系统通知、标记已读）
    // page10: 余额页（查看余额、充值）

    /**
     * 查询钱包余额：
     * 1. 校验用户存在；
     * 2. 若钱包不存在则先兜底创建；
     * 3. 返回当前余额等钱包信息。
     */
    @LogExecution(returnType = DataResp.class)
    @PostMapping("/wallet/balance")
    public DataResp<UserWalletDTO> getWalletBalance(HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        return new DataResp<>(BaseRespConstant.SUC, userInteractionService.getWalletBalance(currentUserId));
    }

    /**
     * 分页查询钱包流水：
     * 1. 校验用户存在；
     * 2. 根据 page/size 查询钱包流水；
     * 3. 返回流水明细和总数。
     */
    @LogExecution(returnType = QueryWalletRecordResp.class)
    @PostMapping("/wallet/records")
    public QueryWalletRecordResp queryWalletRecords(@RequestBody QueryWalletRecordReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        return userInteractionService.queryWalletRecords(currentUserId, req);
    }

    /**
     * 模拟充值：
     * 1. 校验用户和充值金额；
     * 2. 增加钱包余额；
     * 3. 写入一条充值流水；
     * 4. 返回最新钱包信息。
     */
    @Transactional
    @LogExecution(returnType = DataResp.class)
    @PostMapping("/wallet/recharge")
    public DataResp<UserWalletDTO> rechargeWallet(@RequestBody RechargeWalletReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        return new DataResp<>(BaseRespConstant.SUC, userInteractionService.rechargeWallet(currentUserId, req));
    }

    @PostConstruct
    public void init() {
        RBlockingQueue<Long> queue = redissonClient.getBlockingQueue("order:cancel:queue");
        cancelOrderThread = new Thread(() -> {
            while (running) {
                try {
                    Long orderId = queue.poll(1, TimeUnit.SECONDS);
                    if (ObjectUtils.isNotEmpty(orderId)) {
                        handleDelayQueueCancel(orderId);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("[delay-queue] 取消订单处理线程被中断");
                    break;
                } catch (Exception e) {
                    if (running) {
                        log.error("[delay-queue] 取消订单失败", e);
                    }
                }
            }
            log.info("[delay-queue] 取消订单处理线程已停止");
        }, "order-cancel-handler");
        cancelOrderThread.start();
    }

    @PreDestroy
    public void destroy() {
        log.info("[delay-queue] 开始关闭取消订单处理线程");
        running = false;
        if (ObjectUtils.isNotEmpty(cancelOrderThread)) {
            cancelOrderThread.interrupt();
            try {
                cancelOrderThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("[delay-queue] 取消订单处理线程已关闭");
    }

    /**
     * 延迟队列主链路处理超时未支付订单：
     * 1. 查询订单是否还处于待支付状态；
     * 2. 若已支付或已被其他链路处理，则直接跳过；
     * 3. 触发状态机自动取消，并由状态机负责发送通知。
     */
    public void handleDelayQueueCancel(Long orderId) {
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setOrderId(orderId);
        List<ReservationOrder> orders = reservationOrderService.queryByCondition(searchBo);
        if (CollectionUtils.isEmpty(orders)) {
            return;
        }
        ReservationOrder order = orders.get(0);
        if (Objects.equals(order.getPayStatus(), PayStatus.PAID.getCode())) {
            log.info("[delay-queue] orderId={} 已支付，跳过超时取消", orderId);
            return;
        }
        if (!Objects.equals(order.getStatus(), OrderStatus.PENDING_PAYMENT.getCode())) {
            log.info("[delay-queue] orderId={} 状态已变更为{}，跳过超时取消", orderId, order.getStatus());
            return;
        }
        try {
            OrderContext context = new OrderContext(orderId, null, null, "订单超时未支付，系统自动取消");
            context.setSource("delay-queue");
            context.setNotifyEnabled(true);
            orderFlowService.trigger(OrderStatus.PENDING_PAYMENT, OrderStateEvent.TIMEOUT_CANCEL, context);
            log.info("[delay-queue] orderId={} 超时取消成功", orderId);
        } catch (Exception e) {
            log.error("[delay-queue] orderId={} 超时取消失败", orderId, e);
        }
    }



    /**
     * 搜索厨师列表：
     * 1. 根据关键词、服务区域、价格、评分和排序组装查询条件；
     * 2. 分页查询厨师资料列表；
     * 3. 转换成卡片 DTO 返回前端展示。
     */
    @LogExecution(returnType = SearchChefResp.class)
    @PostMapping("/chef/search")
    public SearchChefResp searchChef(@RequestBody SearchChefReq req, HttpServletRequest request) {
        AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        return userQueryService.searchChef(req);
    }

    /**
     * 查询厨师详情：
     * 1. 校验厨师存在；
     * 2. 查询厨师基础资料、服务资料、证件和评分；
     * 3. 转换成详情 DTO 返回前端。
     */
    @LogExecution(returnType = ChefTimeDetailResp.class)
    @PostMapping("/chef/detail")
    public DataResp<ChefDetailDTO> chefDetail(@RequestBody ChefDetailReq req, HttpServletRequest request) {
        AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        return new DataResp<>(BaseRespConstant.SUC, userQueryService.chefDetail(req));
    }

    /**
     * 分页查询厨师可预约时间段：
     * 1. 按厨师 ID 查询可用时间段；
     * 2. 过滤为当前仍有效的时间段；
     * 3. 返回时间段列表和总数。
     */
    @LogExecution(returnType = ChefReviewDetailResp.class)
    @PostMapping("/chef/times")
    public ChefTimeDetailResp chefTimeDetail(@RequestBody ChefTimeDetailReq req, HttpServletRequest request) {
        AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        return userQueryService.chefTimeDetail(req);
    }

    /**
     * 分页查询厨师评价：
     * 1. 按厨师 ID 查询评价；
     * 2. 只返回审核通过的评价；
     * 3. 转换评价 DTO 并返回总数。
     */
    @LogExecution(returnType = DataResp.class)
    @PostMapping("/chef/reviews")
    public ChefReviewDetailResp chefReviewDetail(@RequestBody ChefReviewDetailReq req, HttpServletRequest request) {
        AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        return userQueryService.chefReviewDetail(req);
    }

    /**
     * 创建订单：
     * 1. 校验用户、厨师、时间段、预约开始结束时间和人数；
     * 2. 校验厨师已审核通过且时间段可预约；
     * 3. 检查和已有订单是否冲突；
     * 4. 计算订单金额并创建待支付订单；
     * 5. 将订单加入延迟队列，超时未支付会自动取消。
     */
    @Transactional
    @LogExecution(returnType = DataResp.class)
    @PostMapping("/order/create")
    public DataResp<OrderViewDTO> createOrder(@RequestBody CreateOrderReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        return new DataResp<>(BaseRespConstant.SUC, userOrderService.createOrder(currentUserId, req));
    }

    /**
     * 支付订单：
     * 1. 校验订单归属和当前状态；
     * 2. 仅允许待支付订单支付；
     * 3. 通过状态机完成扣款、更新支付状态和订单流转。
     */
    @Transactional
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/order/pay")
    public SimpleResp payOrder(@RequestBody PayOrderReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        userOrderService.payOrder(currentUserId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }

    /**
     * 取消订单：
     * 1. 校验订单归属、状态和取消原因；
     * 2. 仅允许待支付/待接单订单取消；
     * 3. 通过状态机流转为已取消；
     * 4. 若订单已支付，则同时退款并更新支付状态。
     */
    @Transactional
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/order/cancel")
    public SimpleResp cancelOrder(@RequestBody CancelOrderReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        userOrderService.cancelOrder(currentUserId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }

    /**
     * 创建评论：
     * 1. 校验订单归属和评分范围；
     * 2. 只允许已完成订单评价；
     * 3. 校验一笔订单只能评价一次；
     * 4. 创建待审核评论记录。
     */
    @Transactional
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/review/create")
    public SimpleResp createReview(@RequestBody CreateReviewReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        userInteractionService.createReview(currentUserId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }

    /**
     * 删除评论：
     * 1. 校验评论存在且属于当前用户；
     * 2. 将评论状态更新为已删除，实现逻辑删除。
     */
    @Transactional
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/review/delete")
    public SimpleResp deleteReview(@RequestBody DeleteReviewReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        userInteractionService.deleteReview(currentUserId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }


    /**
     * 创建举报：
     * 1. 校验订单归属和举报原因；
     * 2. 只允许已完成订单举报；
     * 3. 创建一条待处理的举报记录。
     */
    @Transactional
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/report/create")
    public SimpleResp createReport(@RequestBody CreateReportReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        userInteractionService.createReport(currentUserId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }


    /**
     * 分页查询用户订单列表：
     * 1. 根据用户 ID、状态和分页条件查询订单；
     * 2. 组装订单视图 DTO；
     * 3. 返回订单列表和总数。
     */
    @LogExecution(returnType = QueryUserOrderResp.class)
    @PostMapping("/order/list")
    public QueryUserOrderResp orderList(@RequestBody QueryUserOrderReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        return userQueryService.orderList(currentUserId, req);
    }

    /**
     * 查询订单详情：
     * 1. 查询订单是否存在；
     * 2. 校验当前用户或当前厨师是否有权限查看；
     * 3. 返回完整订单详情。
     */
    @LogExecution(returnType = DataResp.class)
    @PostMapping("/order/detail")
    public DataResp<OrderViewDTO> orderDetail(@RequestBody QueryOrderDetailReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        return new DataResp<>(BaseRespConstant.SUC, userQueryService.orderDetail(currentUserId, req));
    }

    /**
     * 分页查询通知列表：
     * 1. 根据用户 ID 查询通知；
     * 2. 支持只看未读和分页查询；
     * 3. 返回通知列表和总数。
     */
    @LogExecution(returnType = ListResp.class)
    @PostMapping("/notification/list")
    public ListResp<NotificationRecord> notificationList(@RequestBody QueryNotificationReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        return userInteractionService.notificationList(currentUserId, req);
    }

    /**
     * 标记通知已读：
     * 1. 校验通知存在且属于当前用户；
     * 2. 将通知状态更新为已读。
     */
    @Transactional
    @LogExecution(returnType = SimpleResp.class)
    @PostMapping("/notification/read")
    public SimpleResp readNotification(@RequestBody ReadNotificationReq req, HttpServletRequest request) {
        Long currentUserId = AuthRequestUtils.requireUser(request, UserRole.NORMAL_USER);
        userInteractionService.readNotification(currentUserId, req);
        return new SimpleResp(BaseRespConstant.SUC);
    }
}
