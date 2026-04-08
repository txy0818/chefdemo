package com.txy.chefdemo.controller.constant;

import com.txy.chefdemo.aspect.LogExecution;
import com.txy.chefdemo.domain.constant.*;
import com.txy.chefdemo.domain.dto.Label;
import com.txy.chefdemo.resp.ListResp;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Slf4j
@RestController
@RequestMapping("/constant")
public class ConstantController {

    /** 返回审核状态枚举，供前端下拉框和状态映射使用。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/auditStatus")
    public ListResp<Label> getAuditStatus() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(AuditStatus.values()));
    }

    /** 返回可预约时间段状态枚举。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/availableTimeStatus")
    public ListResp<Label> getAvailableTimeStatus() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(AvailableTimeStatus.values()));
    }

    /** 返回菜系列表枚举。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/cuisineType")
    public ListResp<Label> getCuisineType() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(CuisineType.values()));
    }

    /** 返回性别枚举。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/gender")
    public ListResp<Label> getGender() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(Gender.values()));
    }

    /** 返回订单状态枚举。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/orderStatus")
    public ListResp<Label> getOrderStatus() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(OrderStatus.values()));
    }

    /** 返回支付方式枚举。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/paymentType")
    public ListResp<Label> getPaymentType() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(PaymentType.values()));
    }

    /** 返回支付状态枚举。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/payStatus")
    public ListResp<Label> getPayStatus() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(PayStatus.values()));
    }

    /** 返回举报状态枚举。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/reportStatus")
    public ListResp<Label> getReportStatus() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(ReportStatus.values()));
    }

    /** 返回评论审核状态枚举。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/reviewAuditStatus")
    public ListResp<Label> getReviewAuditStatus() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(ReviewAuditStatus.values()));
    }

    /** 返回评论状态枚举。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/reviewStatus")
    public ListResp<Label> getReviewStatus() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(ReviewStatus.values()));
    }

    /** 返回用户角色枚举。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/userRole")
    public ListResp<Label> getUserRole() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(UserRole.values()));
    }

    /** 返回用户状态枚举。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/userStatus")
    public ListResp<Label> getUserStatus() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(UserStatus.values()));
    }

    /** 返回通知读取状态枚举。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/notificationReadStatus")
    public ListResp<Label> getNotificationReadStatus() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(NotificationReadStatus.values()));
    }

    /** 返回钱包流水类型枚举。 */
    @LogExecution(returnType = ListResp.class)
    @GetMapping("/walletRecordType")
    public ListResp<Label> getWalletRecordType() {
        return new ListResp<>(BaseRespConstant.SUC, buildLabels(WalletRecordType.values()));
    }

    private List<Label> buildLabels(WalletRecordType[] values) {
        List<Label> labels = new ArrayList<>();
        for (WalletRecordType value : values) {
            labels.add(new Label(value.getDesc(), value.getCode()));
        }
        return labels;
    }

    private List<Label> buildLabels(CuisineType[] values) {
        List<Label> labels = new ArrayList<>();
        for (CuisineType value : values) {
            labels.add(new Label(value.getCuisineName(), value.getCode()));
        }
        return labels;
    }

    private List<Label> buildLabels(UserRole[] values) {
        List<Label> labels = new ArrayList<>();
        for (UserRole value : values) {
            labels.add(new Label(value.getDesc(), value.getCode()));
        }
        return labels;
    }

    private List<Label> buildLabels(Gender[] values) {
        List<Label> labels = new ArrayList<>();
        for (Gender value : values) {
            labels.add(new Label(value.getDesc(), value.getCode()));
        }
        return labels;
    }

    private List<Label> buildLabels(OrderStatus[] values) {
        List<Label> labels = new ArrayList<>();
        for (OrderStatus value : values) {
            labels.add(new Label(value.getDesc(), value.getCode()));
        }
        return labels;
    }

    private List<Label> buildLabels(PayStatus[] values) {
        List<Label> labels = new ArrayList<>();
        for (PayStatus value : values) {
            labels.add(new Label(value.getDesc(), value.getCode()));
        }
        return labels;
    }

    private List<Label> buildLabels(PaymentType[] values) {
        List<Label> labels = new ArrayList<>();
        for (PaymentType value : values) {
            labels.add(new Label(value.getDesc(), value.getCode()));
        }
        return labels;
    }

    private List<Label> buildLabels(AuditStatus[] values) {
        List<Label> labels = new ArrayList<>();
        for (AuditStatus value : values) {
            labels.add(new Label(value.getDesc(), value.getCode()));
        }
        return labels;
    }

    private List<Label> buildLabels(ReviewAuditStatus[] values) {
        List<Label> labels = new ArrayList<>();
        for (ReviewAuditStatus value : values) {
            labels.add(new Label(value.getDesc(), value.getCode()));
        }
        return labels;
    }

    private List<Label> buildLabels(ReviewStatus[] values) {
        List<Label> labels = new ArrayList<>();
        for (ReviewStatus value : values) {
            labels.add(new Label(value.getDesc(), value.getCode()));
        }
        return labels;
    }

    private List<Label> buildLabels(ReportStatus[] values) {
        List<Label> labels = new ArrayList<>();
        for (ReportStatus value : values) {
            labels.add(new Label(value.getDesc(), value.getCode()));
        }
        return labels;
    }

    private List<Label> buildLabels(AvailableTimeStatus[] values) {
        List<Label> labels = new ArrayList<>();
        for (AvailableTimeStatus value : values) {
            labels.add(new Label(value.getDesc(), value.getCode()));
        }
        return labels;
    }

    private List<Label> buildLabels(UserStatus[] values) {
        List<Label> labels = new ArrayList<>();
        for (UserStatus value : values) {
            labels.add(new Label(value.getDesc(), value.getCode()));
        }
        return labels;
    }

    private List<Label> buildLabels(NotificationReadStatus[] values) {
        List<Label> labels = new ArrayList<>();
        for (NotificationReadStatus value : values) {
            labels.add(new Label(value.getDesc(), value.getCode()));
        }
        return labels;
    }
}
