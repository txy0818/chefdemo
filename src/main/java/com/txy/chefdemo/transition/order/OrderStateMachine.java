package com.txy.chefdemo.transition.order;

import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.constant.OrderStatus;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

public class OrderStateMachine {
    private final Map<OrderStatus, Map<OrderStateEvent, OrderTransition>> transitions = new HashMap<>();

    public void addTransition(OrderTransition transition) {
        transitions.computeIfAbsent(transition.getSource(), key -> new HashMap<>())
                .put(transition.getEvent(), transition);
    }

    public ReservationOrder trigger(OrderStatus currentStatus, OrderStateEvent event, OrderContext context) {
        Map<OrderStateEvent, OrderTransition> eventMap = transitions.get(currentStatus);
        if (ObjectUtils.isEmpty(eventMap) || !eventMap.containsKey(event)) {
            throw new IllegalStateException("非法订单状态流转:" + currentStatus + "->" + event);
        }
        OrderTransition transition = eventMap.get(event);
        return transition.getAction().execute(context);
    }
}
