package com.txy.chefdemo.transition.order;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderContext {
    private Long orderId;
    private Long operatorUserId;
    private Integer payType;
    private String reason;
    private Boolean notifyEnabled = true;
    private String source = "";

    public OrderContext(Long orderId, Long operatorUserId, Integer payType, String reason) {
        this.orderId = orderId;
        this.operatorUserId = operatorUserId;
        this.payType = payType;
        this.reason = reason;
    }
}
