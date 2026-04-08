package com.txy.chefdemo.transition.order.impl;

import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.transition.order.OrderAction;
import com.txy.chefdemo.transition.order.OrderContext;
import com.txy.chefdemo.transition.order.support.OrderTransitionSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderAutoCompleteAction implements OrderAction {

    @Autowired
    private OrderTransitionSupport support;

    /**
     * 1. 查询当前订单并确认订单存在。
     * 2. 将订单状态更新为“已完成”，并记录系统自动完成时间。
     * 3. 向用户和厨师发送订单自动完成通知。
     * 4. 回写最新订单状态。
     */
    @Override
    public ReservationOrder execute(OrderContext context) {
        ReservationOrder order = support.queryOrder(context.getOrderId());
        log.info("[{}] orderId={} 进入自动完成处理", context.getSource(), order.getId());
        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setCompleteTime(System.currentTimeMillis());
        ReservationOrder updatedOrder = support.updateOrder(order);
        support.createBothSideNotification(
                updatedOrder,
                "订单状态更新",
                "系统已将超时未完成的订单自动更新为“" + OrderStatus.COMPLETED.getDesc() + "”，欢迎对服务进行评价。订单ID：" + updatedOrder.getId(),
                "订单状态更新",
                "订单在预约结束后5分钟内未手动完成，系统已自动更新为“" + OrderStatus.COMPLETED.getDesc() + "”。订单ID：" + updatedOrder.getId()
        );
        log.info("[{}] orderId={} 自动完成通知已发送", context.getSource(), updatedOrder.getId());
        return updatedOrder;
    }
}
