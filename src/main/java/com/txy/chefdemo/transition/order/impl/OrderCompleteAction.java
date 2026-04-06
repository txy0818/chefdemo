package com.txy.chefdemo.transition.order.impl;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.transition.order.OrderAction;
import com.txy.chefdemo.transition.order.OrderContext;
import com.txy.chefdemo.transition.order.support.OrderTransitionSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OrderCompleteAction implements OrderAction {

    @Autowired
    private OrderTransitionSupport support;

    /**
     * 1. 查询订单并校验当前操作人是否为该订单对应的厨师。
     * 2. 将订单状态更新为“已完成”，并记录完成时间。
     * 3. 向用户和厨师发送订单完成通知。
     * 4. 回写最新订单状态。
     */
    @Override
    public ReservationOrder execute(OrderContext context) {
        ReservationOrder order = support.queryOrder(context.getOrderId());
        Preconditions.checkArgument(Objects.equals(order.getChefId(), context.getOperatorUserId()), BaseRespConstant.FORBIDDEN.getDesc());
        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setCompleteTime(System.currentTimeMillis());
        support.createBothSideNotification(
                order,
                "订单状态更新",
                "您的订单已完成，当前状态为“" + OrderStatus.COMPLETED.getDesc() + "”，欢迎对服务进行评价。订单ID：" + order.getId(),
                "订单状态更新",
                "您已完成本次服务订单，当前状态为“" + OrderStatus.COMPLETED.getDesc() + "”。订单ID：" + order.getId(),
                context.getSource()
        );
        return support.updateOrder(order);
    }
}
