package com.txy.chefdemo.transition.order.impl;

import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.domain.constant.PayStatus;
import com.txy.chefdemo.transition.order.OrderAction;
import com.txy.chefdemo.transition.order.OrderContext;
import com.txy.chefdemo.transition.order.support.OrderTransitionSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderChefFrozenCancelAction implements OrderAction {

    @Autowired
    private OrderTransitionSupport support;

    /**
     * 1. 查询订单并生成“账号状态变更导致关闭订单”的取消原因。
     * 2. 未支付订单直接流转为“已取消”；已支付订单则补退款并将支付状态改为“已退款”。
     * 3. 释放该订单占用的可预约时间段。
     * 4. 向用户和厨师双方发送系统关闭订单通知。
     * 5. 回写最新订单状态。
     * 更新订单，新增用户/厨师通知，更新chef_avaliable_time
     * 说明：
     * 冻结厨师后的资料/评论/举报清理由监听器作为主链路处理，
     * 这里仅负责订单关闭本身，避免状态机 action 额外承担跨模块清理职责。
     */
    @Override
    public ReservationOrder execute(OrderContext context) {
        ReservationOrder order = support.queryOrder(context.getOrderId());
        long now = System.currentTimeMillis();
        String reason = ObjectUtils.defaultIfNull(context.getReason(), "因账号状态变更，订单已由系统关闭。");
        log.info("[{}] orderId={} 进入冻结关闭订单处理", context.getSource(), order.getId());
        
        order.setCancelReason(reason);
        order.setCancelTime(now);
        
        if (order.getStatus().equals(OrderStatus.PENDING_PAYMENT.getCode())) {
            order.setStatus(OrderStatus.CANCELLED.getCode());
            ReservationOrder updatedOrder = support.updateOrder(order);
            support.createBothSideNotification(
                    updatedOrder,
                    "订单状态更新",
                    reason + " 当前状态为“" + OrderStatus.CANCELLED.getDesc() + "”。订单ID：" + updatedOrder.getId(),
                    "订单状态更新",
                    reason + " 当前状态为“" + OrderStatus.CANCELLED.getDesc() + "”。订单ID：" + updatedOrder.getId()
            );
            support.releaseTime(updatedOrder.getChefAvailableTimeId());
            log.info("[{}] orderId={} 冻结关闭订单处理完成", context.getSource(), updatedOrder.getId());
            return updatedOrder;
        } else {
            boolean paid = order.getPayStatus().equals(PayStatus.PAID.getCode());
            order.setStatus(OrderStatus.CANCELLED.getCode());
            if (paid) {
                support.refundIfPaid(order);
                order.setPayStatus(PayStatus.REFUNDED.getCode());
            }
            ReservationOrder updatedOrder = support.updateOrder(order);
            support.createBothSideNotification(
                    updatedOrder,
                    "订单状态更新",
                    reason + " 当前订单状态为“" + OrderStatus.CANCELLED.getDesc() + "”。订单ID：" + updatedOrder.getId(),
                    "订单状态更新",
                    reason + " 当前订单状态为“" + OrderStatus.CANCELLED.getDesc() + "”。订单ID：" + updatedOrder.getId()
            );
            support.releaseTime(updatedOrder.getChefAvailableTimeId());
            log.info("[{}] orderId={} 冻结关闭订单处理完成", context.getSource(), updatedOrder.getId());
            return updatedOrder;
        }
    }
}
