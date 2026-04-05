package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderReq {
    private Long chefUserId;
    private Long chefAvailableTimeId;
    private Long startTime;
    private Long endTime;
    private Integer peopleCount;
    private String specialRequirements;
    private String contactName;
    private String contactPhone;
    private String contactAddress;
}
