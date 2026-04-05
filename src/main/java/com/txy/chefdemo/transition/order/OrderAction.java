package com.txy.chefdemo.transition.order;

import com.txy.chefdemo.domain.ReservationOrder;

@FunctionalInterface
public interface OrderAction {
    ReservationOrder execute(OrderContext context);
}
