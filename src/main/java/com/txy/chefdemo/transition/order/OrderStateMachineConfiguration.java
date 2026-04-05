package com.txy.chefdemo.transition.order;

import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.transition.order.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderStateMachineConfiguration {

    @Autowired
    private OrderPayAction orderPayAction;
    @Autowired
    private OrderUserCancelAction orderUserCancelAction;
    @Autowired
    private OrderChefAcceptAction orderChefAcceptAction;
    @Autowired
    private OrderChefRejectAction orderChefRejectAction;
    @Autowired
    private OrderCompleteAction orderCompleteAction;
    @Autowired
    private OrderAutoCompleteAction orderAutoCompleteAction;
    @Autowired
    private OrderTimeoutCancelAction orderTimeoutCancelAction;
    @Autowired
    private OrderChefFrozenCancelAction orderChefFrozenCancelAction;

    @Bean
    public OrderStateMachine orderStateMachine() {
        OrderStateMachine stateMachine = new OrderStateMachine();
        stateMachine.addTransition(new OrderTransition(OrderStatus.PENDING_PAYMENT, OrderStateEvent.PAY, OrderStatus.PENDING_ACCEPT, orderPayAction));
        stateMachine.addTransition(new OrderTransition(OrderStatus.PENDING_PAYMENT, OrderStateEvent.USER_CANCEL, OrderStatus.CANCELLED, orderUserCancelAction));
        stateMachine.addTransition(new OrderTransition(OrderStatus.PENDING_PAYMENT, OrderStateEvent.TIMEOUT_CANCEL, OrderStatus.CANCELLED, orderTimeoutCancelAction));
        stateMachine.addTransition(new OrderTransition(OrderStatus.PENDING_PAYMENT, OrderStateEvent.CHEF_FROZEN_CANCEL, OrderStatus.CANCELLED, orderChefFrozenCancelAction));
        stateMachine.addTransition(new OrderTransition(OrderStatus.PENDING_ACCEPT, OrderStateEvent.USER_CANCEL, OrderStatus.CANCELLED, orderUserCancelAction));
        stateMachine.addTransition(new OrderTransition(OrderStatus.PENDING_ACCEPT, OrderStateEvent.CHEF_ACCEPT, OrderStatus.ACCEPTED, orderChefAcceptAction));
        stateMachine.addTransition(new OrderTransition(OrderStatus.PENDING_ACCEPT, OrderStateEvent.CHEF_REJECT, OrderStatus.REJECTED, orderChefRejectAction));
        stateMachine.addTransition(new OrderTransition(OrderStatus.PENDING_ACCEPT, OrderStateEvent.CHEF_FROZEN_CANCEL, OrderStatus.CANCELLED, orderChefFrozenCancelAction));
        stateMachine.addTransition(new OrderTransition(OrderStatus.ACCEPTED, OrderStateEvent.COMPLETE, OrderStatus.COMPLETED, orderCompleteAction));
        stateMachine.addTransition(new OrderTransition(OrderStatus.ACCEPTED, OrderStateEvent.AUTO_COMPLETE, OrderStatus.COMPLETED, orderAutoCompleteAction));
        stateMachine.addTransition(new OrderTransition(OrderStatus.ACCEPTED, OrderStateEvent.CHEF_FROZEN_CANCEL, OrderStatus.CANCELLED, orderChefFrozenCancelAction));
        return stateMachine;
    }
}
