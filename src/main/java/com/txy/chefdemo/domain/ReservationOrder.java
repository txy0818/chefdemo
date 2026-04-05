package com.txy.chefdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationOrder {
    /** 订单ID */
    private Long id;
    /** 下单用户ID */
    private Long userId;
    /** 厨师ID */
    private Long chefId;
    /** 预约时间段ID */
    private Long chefAvailableTimeId;
    /** 预约开始时间(毫秒) */
    private Long startTime;
    /** 预约结束时间(毫秒) */
    private Long endTime;
    /** 总时长(毫秒) */
    private Long totalTime;
    /** 订单总金额(分) */
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
    /** 订单状态 1-待支付 2-待接单 3-已接单 4-已拒单 5-已完成 6-已取消 */
    private Integer status;
    /** 支付状态 1-未支付 2-已支付 3-支付失败 4-已退款 */
    private Integer payStatus;
    /** 取消/拒单原因 */
    private String cancelReason;
    /** 支付截止时间(毫秒) */
    private Long payDeadlineTime;
    /** 支付时间(毫秒) */
    private Long payTime;
    /** 接单时间(毫秒) */
    private Long acceptTime;
    /** 完成时间(毫秒) */
    private Long completeTime;
    /** 取消时间(毫秒) */
    private Long cancelTime;
    /** 创建时间(毫秒) */
    private Long createTime;
    /** 更新时间(毫秒) */
    private Long updateTime;
}
