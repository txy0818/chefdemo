package com.txy.chefdemo.service.impl;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.constant.AuthConstant;
import com.txy.chefdemo.domain.ChefAuditRecord;
import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.ChefProfileChange;
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
import com.txy.chefdemo.req.SendChefMessageReq;
import com.txy.chefdemo.req.UpdateUserStatusReq;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.AdminOperationService;
import com.txy.chefdemo.service.ChefAuditRecordService;
import com.txy.chefdemo.service.ChefProfileChangeService;
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
    private ChefProfileChangeService chefProfileChangeService;
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
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && ObjectUtils.isNotEmpty(req.getUserId()), "用户不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(req.getReason()), "原因不能为空");
        String reason = req.getReason().trim();

        User user = userService.queryById(req.getUserId());
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(user), BaseRespConstant.FAIL.getDesc());

        Integer beforeStatus = user.getStatus();
        user.setStatus(req.getStatus());
        user.setUpdateTime(System.currentTimeMillis());
        userService.updateById(user);

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
        if (user.getRole().equals(UserRole.CHEF.getCode()) && req.getStatus().equals(UserStatus.FROZEN.getCode())) {
            publisher.publishEvent(new UserStatusChangeEvent(user.getId(), req.getStatus(), currentAdminId));
        }
    }

    @Override
    @Transactional
    public void auditChef(Long currentAdminId, AuditChefReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && ObjectUtils.isNotEmpty(req.getChefUserId()), "厨师不能为空");
        String reason = StringUtils.trimToEmpty(req.getReason());
        if (Objects.equals(req.getAuditStatus(), AuditStatus.REJECTED.getCode())) {
            Preconditions.checkArgument(StringUtils.isNotBlank(req.getReason()), "拒绝原因不能为空");
        }

        ChefProfile profile = chefProfileService.queryByUserId(req.getChefUserId());
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(profile), "厨师资料不存在");
        ChefAuditRecord record = chefAuditRecordService.queryPendingRecordByChefUserId(req.getChefUserId());
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(record), "厨师资料待审核记录不存在");
        ChefProfileChange change = chefProfileChangeService.queryByUserId(req.getChefUserId());
        boolean auditChange = Objects.equals(profile.getAuditStatus(), AuditStatus.APPROVED.getCode())
                && ObjectUtils.isNotEmpty(change)
                && Objects.equals(change.getAuditStatus(), AuditStatus.PENDING.getCode());

        record.setAuditStatus(req.getAuditStatus());
        long now = System.currentTimeMillis();
        record.setAuditTime(now);
        record.setOperatorId(currentAdminId);
        record.setRejectReason(req.getAuditStatus() == AuditStatus.REJECTED.getCode() ? reason : "");
        chefAuditRecordService.updateById(List.of(record));

        if (auditChange) {
            change.setAuditStatus(req.getAuditStatus());
            change.setRejectReason(req.getAuditStatus() == AuditStatus.REJECTED.getCode() ? reason : "");
            change.setAuditTime(now);
            change.setUpdateTime(now);
            chefProfileChangeService.updateById(change);
            if (Objects.equals(req.getAuditStatus(), AuditStatus.APPROVED.getCode())) {
                applyChangeToProfile(profile, change, now);
                chefProfileService.updateById(profile);
            }
            notifyChefAuditResult(req.getChefUserId(), req.getAuditStatus(), reason, now, true);
            return;
        }

        profile.setAuditStatus(req.getAuditStatus());
        profile.setUpdateTime(now);
        chefProfileService.updateById(profile);
        notifyChefAuditResult(req.getChefUserId(), req.getAuditStatus(), reason, now, false);
    }

    @Override
    @Transactional
    public void sendChefMessage(Long currentAdminId, SendChefMessageReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && ObjectUtils.isNotEmpty(req.getChefUserId()), "厨师不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(req.getTitle()), "标题不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(req.getContent()), "内容不能为空");
        User user = userService.queryById(req.getChefUserId());
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(user) && Objects.equals(user.getRole(), UserRole.CHEF.getCode()), "厨师不存在");
        long now = System.currentTimeMillis();
        NotificationRecord record = buildNotificationRecord(
                req.getChefUserId(),
                req.getTitle().trim(),
                req.getContent().trim(),
                now
        );
        notificationRecordService.insert(record);
        notificationService.notifyUser(record, WebSocketMessageType.NOTIFICATION);
        log.info("adminId={} 向 chefUserId={} 发送通知成功", currentAdminId, req.getChefUserId());
    }

    private void notifyChefAuditResult(Long chefUserId, Integer auditStatus, String reason, long now, boolean changeAudit) {
        AuditStatus status = AuditStatus.getByCode(auditStatus);
        if (ObjectUtils.isEmpty(status)) {
            return;
        }
        String title = changeAudit ? "厨师资料变更审核通知" : "厨师资料审核通知";
        String content;
        if (status == AuditStatus.APPROVED) {
            content = changeAudit
                    ? "您的厨师资料变更已审核通过，新的资料内容已生效。"
                    : "您的厨师资料已审核通过，现在可以使用时间管理和订单管理功能。";
        } else if (status == AuditStatus.REJECTED) {
            content = StringUtils.isNotBlank(reason)
                    ? (changeAudit ? "您的厨师资料变更审核未通过，原因：" + reason : "您的厨师资料审核未通过，原因：" + reason)
                    : (changeAudit ? "您的厨师资料变更审核未通过，请修改后重新提交。" : "您的厨师资料审核未通过，请完善资料后重新提交。");
        } else {
            content = "您的厨师资料审核状态已更新。";
        }
        NotificationRecord record = buildNotificationRecord(chefUserId, title, content, now);
        notificationRecordService.insert(record);
        notificationService.notifyUser(record, WebSocketMessageType.CHEF_AUDIT_RESULT);
    }

    private void notifyUserStatusChange(User user, Integer status, String reason, long now) {
        UserStatus targetStatus = UserStatus.getByCode(status);
        if (ObjectUtils.isEmpty(targetStatus)) {
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
        NotificationRecord record = buildNotificationRecord(user.getId(), title, content, now);
        notificationRecordService.insert(record);
        notificationService.notifyUser(record);

        redissonClient.getBucket(AuthConstant.LOGIN_TOKEN_KEY_PREFIX + user.getId()).delete();
        notificationService.forceLogout(user.getId(), title, content);

    }

    private NotificationRecord buildNotificationRecord(Long userId, String title, String content, long now) {
        return new NotificationRecord(
                null,
                userId,
                title,
                content,
                NotificationReadStatus.UNREAD.getCode(),
                now,
                now
        );
    }

    private void applyChangeToProfile(ChefProfile profile, ChefProfileChange change, long now) {
        profile.setAvatar(change.getAvatar());
        profile.setDisplayName(change.getDisplayName());
        profile.setPhone(change.getPhone());
        profile.setRealName(change.getRealName());
        profile.setIdCardImgs(change.getIdCardImgs());
        profile.setHealthCertImgs(change.getHealthCertImgs());
        profile.setChefCertImgs(change.getChefCertImgs());
        profile.setCuisineType(change.getCuisineType());
        profile.setServiceArea(change.getServiceArea());
        profile.setServiceDesc(change.getServiceDesc());
        profile.setPrice(change.getPrice());
        profile.setMinPeople(change.getMinPeople());
        profile.setMaxPeople(change.getMaxPeople());
        profile.setAge(change.getAge());
        profile.setGender(change.getGender());
        profile.setWorkYears(change.getWorkYears());
        profile.setAuditStatus(AuditStatus.APPROVED.getCode());
        profile.setUpdateTime(now);
    }
}
