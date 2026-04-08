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
public class OrderChefRejectAction implements OrderAction {

    private static final String AUTO_REJECT_SOURCE = "schedule-auto-reject";

    @Autowired
    private OrderTransitionSupport support;

    /**
     * 1. 查询订单并校验当前操作人是否为该订单对应的厨师。
     * 2. 将订单状态更新为“已拒单”，并记录拒单原因与取消时间。
     * 3. 若订单已支付，则执行退款并将支付状态改为“已退款”。
     * 4. 释放订单占用的可预约时间段。
     * 5. 向用户和厨师双方发送拒单与退款通知。
     * 6. 回写最新订单状态。
     */
    @Override
    public ReservationOrder execute(OrderContext context) {
        ReservationOrder order = support.queryOrder(context.getOrderId());
        boolean autoReject = Objects.equals(AUTO_REJECT_SOURCE, context.getSource());
        Preconditions.checkArgument(autoReject || Objects.equals(order.getChefId(), context.getOperatorUserId()), BaseRespConstant.FORBIDDEN.getDesc());
        boolean paid = Objects.equals(order.getPayStatus(), PayStatus.PAID.getCode());
        order.setStatus(OrderStatus.REJECTED.getCode());
        order.setCancelReason(context.getReason());
        order.setCancelTime(System.currentTimeMillis());
        support.releaseTime(order.getChefAvailableTimeId());
        if (paid) {
            support.refundIfPaid(order);
            order.setPayStatus(PayStatus.REFUNDED.getCode());
        }
        ReservationOrder updatedOrder = support.updateOrder(order);
        if (autoReject) {
            support.createBothSideNotification(
                    updatedOrder,
                    "订单状态更新",
                    paid
                            ? "厨师未在规定时间内接单，系统已自动拒单，退款已退回钱包，当前订单状态为“" + OrderStatus.REJECTED.getDesc() + "”。订单ID：" + updatedOrder.getId()
                            : "厨师未在规定时间内接单，系统已自动拒单，当前订单状态为“" + OrderStatus.REJECTED.getDesc() + "”。订单ID：" + updatedOrder.getId(),
                    "订单状态更新",
                    paid
                            ? "您未在规定时间内接单，系统已自动拒单并完成退款，当前订单状态为“" + OrderStatus.REJECTED.getDesc() + "”。订单ID：" + updatedOrder.getId()
                            : "您未在规定时间内接单，系统已自动拒单，当前订单状态为“" + OrderStatus.REJECTED.getDesc() + "”。订单ID：" + updatedOrder.getId()
            );
            return updatedOrder;
        }
        support.createBothSideNotification(
                updatedOrder,
                "订单状态更新",
                "厨师已拒绝接单，退款已退回钱包，当前订单状态为“" + OrderStatus.REJECTED.getDesc() + "”。订单ID：" + updatedOrder.getId(),
                "订单状态更新",
                "您已拒绝该订单，系统已完成退款，当前订单状态为“" + OrderStatus.REJECTED.getDesc() + "”。订单ID：" + updatedOrder.getId()
        );
        return updatedOrder;
    }
}
