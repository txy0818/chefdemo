package com.txy.chefdemo.transition.order.impl;

import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.transition.order.OrderAction;
import com.txy.chefdemo.transition.order.OrderContext;
import com.txy.chefdemo.transition.order.support.OrderTransitionSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderTimeoutCancelAction implements OrderAction {

    @Autowired
    private OrderTransitionSupport support;

    /**
     * 1. 查询订单并确认订单仍处于待支付状态。
     * 2. 将订单状态更新为“已取消”，并写入系统超时取消原因。
     * 3. 释放该订单占用的可预约时间段。
     * 4. 向用户和厨师发送超时取消通知。
     * 5. 回写最新订单状态。
     */
    @Override
    public ReservationOrder execute(OrderContext context) {
        ReservationOrder order = support.queryOrder(context.getOrderId());
        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelReason("订单5分钟内未支付，系统自动取消");
        order.setCancelTime(System.currentTimeMillis());
        support.releaseTime(order.getChefAvailableTimeId());
        if (!Boolean.FALSE.equals(context.getNotifyEnabled())) {
            support.createBothSideNotification(
                    order,
                    "订单状态更新",
                    "您的订单因5分钟内未支付已自动取消，当前状态为“" + OrderStatus.CANCELLED.getDesc() + "”。订单ID：" + order.getId(),
                    "订单状态更新",
                    "订单因用户5分钟内未支付已自动取消，当前状态为“" + OrderStatus.CANCELLED.getDesc() + "”。订单ID：" + order.getId()
            );
        }
        return support.updateOrder(order);
    }
}
