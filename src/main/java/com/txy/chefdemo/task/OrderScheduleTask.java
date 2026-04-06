package com.txy.chefdemo.task;

import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.WalletRecord;
import com.txy.chefdemo.domain.bo.ReservationOrderSearchBo;
import com.txy.chefdemo.domain.bo.UserSearchBo;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.domain.constant.PayStatus;
import com.txy.chefdemo.domain.constant.UserRole;
import com.txy.chefdemo.domain.constant.UserStatus;
import com.txy.chefdemo.domain.constant.WalletRecordType;
import com.txy.chefdemo.service.FrozenChefCleanupService;
import com.txy.chefdemo.service.OrderFlowService;
import com.txy.chefdemo.service.ReservationOrderService;
import com.txy.chefdemo.service.UserService;
import com.txy.chefdemo.service.WalletRecordService;
import com.txy.chefdemo.transition.order.OrderContext;
import com.txy.chefdemo.transition.order.OrderStateEvent;
import com.txy.chefdemo.transition.order.support.OrderTransitionSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrderScheduleTask {

    @Autowired
    private ReservationOrderService reservationOrderService;
    @Autowired
    private OrderFlowService orderFlowService;
    @Autowired
    private OrderTransitionSupport orderTransitionSupport;
    @Autowired
    private WalletRecordService walletRecordService;
    @Autowired
    private UserService userService;
    @Autowired
    private FrozenChefCleanupService frozenChefCleanupService;

    /**
     * 1. 每分钟扫描所有待支付订单。
     * 2. 判断是否已超过支付截止时间。
     * 3. 对超时未支付订单触发状态机，自动流转为“已取消”。
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void autoCancelTimeoutOrders() {
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        List<ReservationOrder> reservationOrders = reservationOrderService.queryByCondition(searchBo);
        long now = System.currentTimeMillis();
        for (ReservationOrder order : reservationOrders) {
            if (order.getPayDeadlineTime() == null || order.getPayDeadlineTime() > now) {
                continue;
            }
            try {
                OrderContext context = new OrderContext(order.getId(), order.getUserId(), null, "系统超时取消");
                context.setSource("schedule-scan");
                context.setNotifyEnabled(false);
                orderFlowService.trigger(OrderStatus.fromCode(order.getStatus()),
                        OrderStateEvent.TIMEOUT_CANCEL,
                        context);
                log.info("[schedule-scan] orderId={} 超时取消补偿成功", order.getId());
            } catch (Exception e) {
                log.error("[schedule-scan] 自动取消超时订单失败, orderId={}", order.getId(), e);
            }
        }
    }

    /**
     * 1. 每分钟扫描所有已接单订单。
     * 2. 判断订单结束时间后是否已超过5分钟且仍未手动完成。
     * 3. 对满足条件的订单触发状态机，系统自动流转为“已完成”。
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void autoCompleteTimeoutAcceptedOrders() {
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setStatus(OrderStatus.ACCEPTED.getCode());
        List<ReservationOrder> reservationOrders = reservationOrderService.queryByCondition(searchBo);
        long now = System.currentTimeMillis();
        for (ReservationOrder order : reservationOrders) {
            if (order.getEndTime() == null || order.getEndTime() + 5 * 60 * 1000L > now) {
                continue;
            }
            try {
                OrderContext context = new OrderContext(order.getId(), null, null, "订单结束5分钟后系统自动完成");
                context.setSource("schedule-auto-complete");
                orderFlowService.trigger(OrderStatus.fromCode(order.getStatus()),
                        OrderStateEvent.AUTO_COMPLETE,
                        context);
                log.info("[schedule-auto-complete] orderId={} 自动完成成功", order.getId());
            } catch (Exception e) {
                log.error("[schedule-auto-complete] 自动完成超时订单失败, orderId={}", order.getId(), e);
            }
        }
    }

    /**
     * 1. 每分钟扫描已拒单、已取消这类理论上需要退款的订单。
     * 2. 若订单仍是“已支付”状态，则检查是否已经存在退款流水。
     * 3. 没有退款流水时系统自动补退款；已有退款流水时仅修正支付状态为“已退款”。
     * 4. 退款或修正成功后，同时补发用户端和厨师端通知。
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void autoRefundFinalOrders() {
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setStatuses(List.of(OrderStatus.REJECTED.getCode(), OrderStatus.CANCELLED.getCode()));
        List<ReservationOrder> reservationOrders = reservationOrderService.queryByCondition(searchBo);
        for (ReservationOrder order : reservationOrders) {
            if (order.getId() == null || order.getPayStatus() == null || order.getPayStatus() != PayStatus.PAID.getCode()) {
                continue;
            }
            try {
                List<WalletRecord> walletRecords = walletRecordService.queryByOrderId(order.getId());
                boolean refunded = walletRecords.stream()
                        .anyMatch(record -> record.getType() != null && record.getType() == WalletRecordType.REFUND.getCode());
                if (refunded) {
                    order.setPayStatus(PayStatus.REFUNDED.getCode());
                    orderTransitionSupport.updateOrder(order);
                    log.info("[schedule-auto-refund] orderId={} 已存在退款流水，仅修正支付状态", order.getId());
                    continue;
                }
                orderTransitionSupport.refundIfPaid(order);
                order.setPayStatus(PayStatus.REFUNDED.getCode());
                orderTransitionSupport.updateOrder(order);
                orderTransitionSupport.createBothSideNotification(
                        order,
                        "订单退款通知",
                        "系统检测到该订单未完成退款，现已自动补退款。订单ID：" + order.getId(),
                        "订单退款通知",
                        "系统已为该订单自动补退款。订单ID：" + order.getId(),
                        "schedule-auto-refund"
                );
                log.info("[schedule-auto-refund] orderId={} 自动补退款成功", order.getId());
            } catch (Exception e) {
                log.error("[schedule-auto-refund] 自动补退款失败, orderId={}", order.getId(), e);
            }
        }
    }

    /**
     * 1. 每分钟扫描所有已冻结的厨师账号。
     * 2. 作为纯补偿链路，补做其仍处于待审核中的资料、评价和举报清理。
     * 3. 补偿处理仍未关闭的待支付/待接单/已接单订单，统一触发“厨师冻结关闭订单”状态机事件。
     *
     * 说明：
     * 冻结厨师的主处理链路在 UserStatusChangeListener 中，
     * 这里仅用于兜底，避免监听器异常或漏执行时数据长期不一致。
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void compensateFrozenChefPendingData() {
        UserSearchBo userSearchBo = new UserSearchBo();
        userSearchBo.setRole(UserRole.CHEF.getCode());
        userSearchBo.setStatus(UserStatus.FROZEN.getCode());
        List<User> frozenChefs = userService.queryUserListByCondition(userSearchBo);
        if (frozenChefs == null || frozenChefs.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        for (User frozenChef : frozenChefs) {
            try {
                Long systemOperatorId = frozenChefCleanupService.getSystemOperatorId(now);
                frozenChefCleanupService.cleanupFrozenChef(frozenChef.getId(), systemOperatorId, now);
                compensateFrozenChefOrders(frozenChef, systemOperatorId);
                log.info("[schedule-frozen-compensate] chefUserId={} 冻结厨师补偿处理完成", frozenChef.getId());
            } catch (Exception e) {
                log.error("[schedule-frozen-compensate] 冻结厨师补偿处理失败, chefUserId={}", frozenChef.getId(), e);
            }
        }
    }

    private void compensateFrozenChefOrders(User frozenChef, Long operatorId) {
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setChefId(frozenChef.getId());
        searchBo.setStatuses(List.of(
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.PENDING_ACCEPT.getCode(),
                OrderStatus.ACCEPTED.getCode()
        ));
        List<ReservationOrder> openOrders = reservationOrderService.queryByCondition(searchBo);
        if (openOrders == null || openOrders.isEmpty()) {
            return;
        }
        for (ReservationOrder order : openOrders) {
            try {
                OrderContext context = new OrderContext(order.getId(), operatorId, null, buildFrozenCancelReason(frozenChef, order));
                context.setSource("schedule-frozen-compensate");
                orderFlowService.trigger(OrderStatus.fromCode(order.getStatus()),
                        OrderStateEvent.CHEF_FROZEN_CANCEL,
                        context);
                log.info("[schedule-frozen-compensate] chefUserId={}, orderId={} 订单补偿关闭成功", frozenChef.getId(), order.getId());
            } catch (Exception e) {
                log.error("[schedule-frozen-compensate] 冻结厨师订单补偿失败, chefUserId={}, orderId={}", frozenChef.getId(), order.getId(), e);
            }
        }
    }

    private String buildFrozenCancelReason(User frozenChef, ReservationOrder order) {
        String suffix = order.getPayStatus() != null && order.getPayStatus() == PayStatus.PAID.getCode()
                ? "订单" + OrderStatus.CANCELLED.getDesc() + "并" + PayStatus.REFUNDED.getDesc()
                : "订单" + OrderStatus.CANCELLED.getDesc();
        return frozenChef.getUsername() + " 厨师账号被冻结，" + suffix;
    }
}
