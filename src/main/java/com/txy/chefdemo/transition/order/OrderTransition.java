package com.txy.chefdemo.transition.order;

import com.txy.chefdemo.domain.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderTransition {
    private final OrderStatus source;
    private final OrderStateEvent event;
    private final OrderStatus target;
    private final OrderAction action;
}
