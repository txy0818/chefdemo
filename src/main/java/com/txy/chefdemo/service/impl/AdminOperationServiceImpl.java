package com.txy.chefdemo.service.impl;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.constant.AuthConstant;
import com.txy.chefdemo.domain.ChefAuditRecord;
import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.NotificationRecord;
import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.UserStatusRecord;
import com.txy.chefdemo.domain.constant.AuditStatus;
import com.txy.chefdemo.domain.constant.NotificationReadStatus;
import com.txy.chefdemo.domain.constant.UserRole;
import com.txy.chefdemo.domain.constant.UserStatus;
import com.txy.chefdemo.domain.constant.WebSocketMessageType;
import com.txy.chefdemo.event.UserStatusChangeEvent;
import com.txy.chefdemo.req.AuditChefReq;
import com.txy.chefdemo.req.UpdateUserStatusReq;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.AdminOperationService;
import com.txy.chefdemo.service.ChefAuditRecordService;
import com.txy.chefdemo.service.ChefProfileService;
import com.txy.chefdemo.service.NotificationRecordService;
import com.txy.chefdemo.service.NotificationService;
import com.txy.chefdemo.service.UserService;
import com.txy.chefdemo.service.UserStatusRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AdminOperationServiceImpl implements AdminOperationService {

    @Autowired
    private UserService userService;
    @Autowired
    private ChefProfileService chefProfileService;
    @Autowired
    private ChefAuditRecordService chefAuditRecordService;
    @Autowired
    private UserStatusRecordService userStatusRecordService;
    @Autowired
    private NotificationRecordService notificationRecordService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    @Transactional
    public void updateUserStatus(Long currentAdminId, UpdateUserStatusReq req) {
        Preconditions.checkArgument(req != null && req.getUserId() != null, "用户不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(req.getReason()), "原因不能为空");
        String reason = req.getReason().trim();

        User user = userService.queryById(req.getUserId());
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(user), BaseRespConstant.FAIL.getDesc());

        Integer beforeStatus = user.getStatus();
        user.setStatus(req.getStatus());
        user.setUpdateTime(System.currentTimeMillis());
        userService.upsert(user);

        redissonClient.getBucket(AuthConstant.LOGIN_TOKEN_KEY_PREFIX + req.getUserId()).delete();

        long now = System.currentTimeMillis();
        UserStatusRecord userStatusRecord = new UserStatusRecord();
        userStatusRecord.setUserId(user.getId());
        userStatusRecord.setBeforeStatus(beforeStatus);
        userStatusRecord.setAfterStatus(req.getStatus());
        userStatusRecord.setReason(reason);
        userStatusRecord.setOperatorId(currentAdminId);
        userStatusRecord.setCreateTime(now);
        userStatusRecordService.insert(userStatusRecord);

        notifyUserStatusChange(user, req.getStatus(), reason, now);
        publisher.publishEvent(new UserStatusChangeEvent(user.getId(), req.getStatus(), currentAdminId));
    }

    @Override
    @Transactional
    public void auditChef(Long currentAdminId, AuditChefReq req) {
        Preconditions.checkArgument(req != null && req.getChefUserId() != null, "厨师不能为空");
        String reason = StringUtils.trimToEmpty(req.getReason());
        if (Objects.equals(req.getAuditStatus(), AuditStatus.REJECTED.getCode())) {
            Preconditions.checkArgument(StringUtils.isNotBlank(req.getReason()), "拒绝原因不能为空");
        }

        ChefProfile profile = chefProfileService.queryByUserId(req.getChefUserId());
        Preconditions.checkArgument(profile != null, "厨师资料不存在");
        ChefAuditRecord record = chefAuditRecordService.queryPendingRecordByChefUserId(req.getChefUserId());
        Preconditions.checkArgument(record != null, "厨师资料待审核记录不存在");

        record.setAuditStatus(req.getAuditStatus());
        long now = System.currentTimeMillis();
        record.setAuditTime(now);
        record.setOperatorId(currentAdminId);
        record.setRejectReason(req.getAuditStatus() == AuditStatus.REJECTED.getCode() ? reason : "");
        chefAuditRecordService.updateById(List.of(record));

        profile.setAuditStatus(req.getAuditStatus());
        profile.setUpdateTime(now);
        chefProfileService.upsert(profile);
        notifyChefAuditResult(req.getChefUserId(), req.getAuditStatus(), reason, now);
    }

    private void notifyChefAuditResult(Long chefUserId, Integer auditStatus, String reason, long now) {
        AuditStatus status = AuditStatus.getByCode(auditStatus);
        if (status == null) {
            return;
        }
        String title = "厨师资料审核通知";
        String content;
        if (status == AuditStatus.APPROVED) {
            content = "您的厨师资料已审核通过，现在可以使用时间管理和订单管理功能。";
        } else if (status == AuditStatus.REJECTED) {
            content = StringUtils.isNotBlank(reason)
                    ? "您的厨师资料审核未通过，原因：" + reason
                    : "您的厨师资料审核未通过，请完善资料后重新提交。";
        } else {
            content = "您的厨师资料审核状态已更新。";
        }
        NotificationRecord record = buildNotificationRecord(chefUserId, title, content, chefUserId, now);
        notificationRecordService.insert(record);
        notificationService.notifyUser(record, WebSocketMessageType.CHEF_AUDIT_RESULT);
    }

    private void notifyUserStatusChange(User user, Integer status, String reason, long now) {
        UserStatus targetStatus = UserStatus.getByCode(status);
        if (targetStatus == null) {
            return;
        }
        String title;
        String content;
        if (targetStatus == UserStatus.FROZEN) {
            title = "账号冻结通知";
            content = StringUtils.isNotBlank(reason)
                    ? "您的账号已被冻结，原因：" + reason
                    : "您的账号已被冻结，请联系管理员处理。";
        } else {
            title = "账号恢复通知";
            content = "您的账号已恢复正常，可以重新登录并继续使用系统。";
        }
        NotificationRecord record = buildNotificationRecord(user.getId(), title, content, user.getId(), now);
        notificationRecordService.insert(record);
        notificationService.notifyUser(record);
        if (targetStatus == UserStatus.FROZEN && user.getRole() != null && user.getRole() != UserRole.ADMIN.getCode()) {
            redissonClient.getBucket(AuthConstant.LOGIN_TOKEN_KEY_PREFIX + user.getId()).delete();
            notificationService.forceLogout(user.getId(), title, content);
        }
    }

    private NotificationRecord buildNotificationRecord(Long userId, String title, String content, Long bizId, long now) {
        return new NotificationRecord(
                null,
                userId,
                title,
                content,
                bizId,
                NotificationReadStatus.UNREAD.getCode(),
                now,
                now
        );
    }
}
