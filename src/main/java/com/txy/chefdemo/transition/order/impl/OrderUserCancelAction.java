package com.txy.chefdemo.transition.order.impl;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.domain.constant.PayStatus;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.transition.order.OrderAction;
import com.txy.chefdemo.transition.order.OrderContext;
import com.txy.chefdemo.transition.order.support.OrderTransitionSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OrderUserCancelAction implements OrderAction {

    @Autowired
    private OrderTransitionSupport support;

    /**
     * 1. 查询订单并校验当前操作人是否为下单用户本人。
     * 2. 写入取消原因和取消时间，并将订单状态更新为“已取消”。
     * 3. 若订单已支付，则执行退款并把支付状态改为“已退款”。
     * 4. 释放该订单占用的可预约时间段。
     * 5. 向用户和厨师双方发送取消订单通知。
     * 6. 回写最新订单状态。
     */
    @Override
    public ReservationOrder execute(OrderContext context) {
        ReservationOrder order = support.queryOrder(context.getOrderId());
        Preconditions.checkArgument(Objects.equals(order.getUserId(), context.getOperatorUserId()), BaseRespConstant.FORBIDDEN.getDesc());

        long now = System.currentTimeMillis();
        order.setCancelReason(context.getReason());
        order.setCancelTime(now);
        if (Objects.equals(order.getPayStatus(), PayStatus.PAID.getCode())) {
            order.setStatus(OrderStatus.CANCELLED.getCode());
            order.setPayStatus(PayStatus.REFUNDED.getCode());
            support.refundIfPaid(order);
            support.createBothSideNotification(
                    order,
                    "订单状态更新",
                    "您的订单已取消，退款已退回钱包，当前订单状态为“" + OrderStatus.CANCELLED.getDesc() + "”。订单ID：" + order.getId(),
                    "订单状态更新",
                    "用户已取消订单，系统已完成退款，当前订单状态为“" + OrderStatus.CANCELLED.getDesc() + "”。订单ID：" + order.getId()
            );
        } else {
            order.setStatus(OrderStatus.CANCELLED.getCode());
            support.createBothSideNotification(
                    order,
                    "订单状态更新",
                    "您的订单已取消，当前状态为“" + OrderStatus.CANCELLED.getDesc() + "”。订单ID：" + order.getId(),
                    "订单状态更新",
                    "用户已取消订单，当前状态为“" + OrderStatus.CANCELLED.getDesc() + "”。订单ID：" + order.getId()
            );
        }
        support.releaseTime(order.getChefAvailableTimeId());
        return support.updateOrder(order);
    }
}
