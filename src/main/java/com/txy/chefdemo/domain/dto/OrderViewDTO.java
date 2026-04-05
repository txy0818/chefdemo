package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单视图DTO - 用于用户查看订单详情
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderViewDTO {
    /** 订单ID */
    private Long id;
    /** 下单用户名 */
    private String userName;
    /** 厨师名 */
    private String chefName;
    /** 所属预约时间段ID */
    private Long chefAvailableTimeId;
    /** 所属预约时间段开始时间 */
    private Long chefAvailableStartTime;
    /** 所属预约时间段结束时间 */
    private Long chefAvailableEndTime;
    /** 预约开始时间 */
    private Long startTime;
    /** 预约结束时间 */
    private Long endTime;
    /** 订单总金额(元) */
    private Long totalAmount;
    /** 预约人数 */
    private Integer peopleCount;
    /** 特殊要求 */
    private String specialRequirements;
    /** 联系人 */
    private String contactName;
    /** 联系电话 */
    private String contactPhone;
    /** 联系地址 */
    private String contactAddress;
    /** 订单状态描述 */
    private Integer status;
    /** 订单状态描述 */
    private String statusDesc;
    /** 支付状态描述 */
    private Integer payStatus;
    /** 支付状态描述 */
    private String payStatusDesc;
    /** 取消原因 */
    private String cancelReason;
    /** 支付截止时间 */
    private Long payDeadlineTime;
}
