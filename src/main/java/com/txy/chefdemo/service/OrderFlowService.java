package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.transition.order.OrderContext;
import com.txy.chefdemo.transition.order.OrderStateEvent;
import com.txy.chefdemo.transition.order.OrderStateMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderFlowService {

    @Autowired
    private OrderStateMachine orderStateMachine;

    @Transactional(rollbackFor = Exception.class)
    public ReservationOrder trigger(OrderStatus currentStatus, OrderStateEvent event, OrderContext context) {
        return orderStateMachine.trigger(currentStatus, event, context);
    }
}
