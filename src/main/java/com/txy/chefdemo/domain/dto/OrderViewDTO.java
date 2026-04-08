package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 订单展示 DTO。 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderViewDTO {
    /** 订单 ID。 */
    private Long id;
    /** 下单用户名。 */
    private String userName;
    /** 厨师用户名。 */
    private String chefName;
    /** 所属预约时间段 ID。 */
    private Long chefAvailableTimeId;
    /** 所属预约时间段开始时间，毫秒时间戳。 */
    private Long chefAvailableStartTime;

    /** 所属预约时间段描述。 */
    private String chefAvailableTimeDesc;

    /** 所属预约时间段结束时间，毫秒时间戳。 */
    private Long chefAvailableEndTime;

    /** 所属预约时间段状态描述。 */
    private String chefAvailableTimeStatusDesc;

    /** 预约开始时间，毫秒时间戳。 */
    private Long startTime;

    /** 预约开始时间描述。 */
    private String startTimeDesc;

    /** 预约结束时间，毫秒时间戳。 */
    private Long endTime;

    /** 预约结束时间描述。 */
    private String endTimeDesc;

    /** 订单总金额，单位为分。 */
    private Long totalAmount;

    /** 订单总金额描述，金额以元为单位。 */
    private String totalAmountDesc;

    /** 预约人数。 */
    private Integer peopleCount;

    /** 特殊要求。 */
    private String specialRequirements;

    /** 联系人。 */
    private String contactName;

    /** 联系电话。 */
    private String contactPhone;

    /** 联系地址。 */
    private String contactAddress;

    /** 订单状态编码。 */
    private Integer status;

    /** 订单状态描述。 */
    private String statusDesc;

    /** 支付状态编码。 */
    private Integer payStatus;

    /** 支付状态描述。 */
    private String payStatusDesc;

    /** 取消原因。 */
    private String cancelReason;

    /** 支付截止时间，毫秒时间戳。 */
    private Long payDeadlineTime;

    /** 支付截止时间描述。 */
    private String payDeadlineTimeDesc;
}
