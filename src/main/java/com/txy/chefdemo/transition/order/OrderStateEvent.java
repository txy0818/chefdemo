package com.txy.chefdemo.transition.order;

public enum OrderStateEvent {
    PAY,
    USER_CANCEL,
    CHEF_ACCEPT,
    CHEF_REJECT,
    COMPLETE,
    AUTO_COMPLETE,
    TIMEOUT_CANCEL,
    CHEF_FROZEN_CANCEL
}
