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
public class OrderChefAcceptAction implements OrderAction {

    @Autowired
    private OrderTransitionSupport support;

    /**
     * 1. 查询订单并校验当前操作人是否为该订单对应的厨师。
     * 2. 将订单状态更新为“已接单”，并记录接单时间。
     * 3. 向用户和厨师发送接单成功通知。
     * 4. 回写最新订单状态。
     */
    @Override
    public ReservationOrder execute(OrderContext context) {
        ReservationOrder order = support.queryOrder(context.getOrderId());
        Preconditions.checkArgument(Objects.equals(order.getChefId(), context.getOperatorUserId()), BaseRespConstant.FORBIDDEN.getDesc());
        order.setStatus(OrderStatus.ACCEPTED.getCode());
        order.setAcceptTime(System.currentTimeMillis());
        support.createBothSideNotification(
                order,
                "订单状态更新",
                "您的订单已被厨师接单，当前状态为“" + OrderStatus.ACCEPTED.getDesc() + "”。订单ID：" + order.getId(),
                "订单状态更新",
                "您已接单成功，当前状态为“" + OrderStatus.ACCEPTED.getDesc() + "”。订单ID：" + order.getId(),
                context.getSource()
        );
        return support.updateOrder(order);
    }
}
