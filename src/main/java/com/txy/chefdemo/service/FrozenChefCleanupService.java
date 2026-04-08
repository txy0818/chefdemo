package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.ChefAuditRecord;
import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.ChefProfileChange;
import com.txy.chefdemo.domain.Report;
import com.txy.chefdemo.domain.Review;
import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.bo.ReportSearchBo;
import com.txy.chefdemo.domain.bo.ReviewSearchBo;
import com.txy.chefdemo.domain.constant.AuditStatus;
import com.txy.chefdemo.domain.constant.ReportStatus;
import com.txy.chefdemo.domain.constant.UserRole;
import com.txy.chefdemo.domain.constant.UserStatus;
import com.txy.chefdemo.utils.PasswordUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class FrozenChefCleanupService {

    public static final String SYSTEM_USERNAME = "system";
    private static final String FROZEN_REJECT_REASON = "账号已冻结，系统自动驳回";

    @Autowired
    private ChefAuditRecordService chefAuditRecordService;
    @Autowired
    private ChefProfileService chefProfileService;
    @Autowired
    private ChefProfileChangeService chefProfileChangeService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private UserService userService;

    @Transactional
    public void cleanupFrozenChef(Long chefUserId, Long operatorId, long now) {
        // 获取 system 的 管理员
        Long systemOperatorId = resolveSystemOperatorId(operatorId, now);
        rejectPendingChefProfile(chefUserId, systemOperatorId, now);
        rejectPendingReviews(chefUserId, now);
        rejectPendingReports(chefUserId, systemOperatorId, now);
    }

    public Long getSystemOperatorId(long now) {
        return resolveSystemOperatorId(0L, now);
    }

    private void rejectPendingChefProfile(Long chefUserId, Long operatorId, long now) {
        ChefAuditRecord pendingRecord = chefAuditRecordService.queryPendingRecordByChefUserId(chefUserId);
        ChefProfile profile = chefProfileService.queryByUserId(chefUserId);
        ChefProfileChange change = chefProfileChangeService.queryByUserId(chefUserId);
        if (ObjectUtils.isNotEmpty(pendingRecord)) {
            pendingRecord.setAuditStatus(AuditStatus.REJECTED.getCode());
            pendingRecord.setRejectReason(FROZEN_REJECT_REASON);
            pendingRecord.setAuditTime(now);
            pendingRecord.setOperatorId(ObjectUtils.defaultIfNull(operatorId, 0L));
            chefAuditRecordService.updateById(List.of(pendingRecord));
        }

        if (ObjectUtils.isNotEmpty(change)) {
            change.setAuditStatus(AuditStatus.REJECTED.getCode());
            change.setRejectReason(FROZEN_REJECT_REASON);
            change.setAuditTime(now);
            change.setUpdateTime(now);
            chefProfileChangeService.updateById(change);
        }

        if (ObjectUtils.isNotEmpty(profile)) {
            profile.setAuditStatus(AuditStatus.REJECTED.getCode());
            profile.setUpdateTime(now);
            chefProfileService.updateById(profile);
        }
    }

    private void rejectPendingReviews(Long chefUserId, long now) {
        ReviewSearchBo reviewSearchBo = new ReviewSearchBo();
        reviewSearchBo.setChefId(chefUserId);
        reviewSearchBo.setAuditStatus(AuditStatus.PENDING.getCode());
        List<Review> reviews = reviewService.queryByCondition(reviewSearchBo);
        if (CollectionUtils.isEmpty(reviews)) {
            return;
        }
        for (Review review : reviews) {
            review.setAuditStatus(AuditStatus.REJECTED.getCode());
            review.setAuditReason(FROZEN_REJECT_REASON);
            review.setUpdateTime(now);
            reviewService.updateById(review);
        }
    }

    private void rejectPendingReports(Long chefUserId, Long operatorId, long now) {
        ReportSearchBo reportSearchBo = new ReportSearchBo();
        reportSearchBo.setTargetUserId(chefUserId);
        reportSearchBo.setStatus(ReportStatus.PENDING.getCode());
        List<Report> reports = reportService.queryByCondition(reportSearchBo);
        if (CollectionUtils.isEmpty(reports)) {
            return;
        }
        for (Report report : reports) {
            report.setStatus(ReportStatus.REJECTED.getCode());
            report.setProcessResult(FROZEN_REJECT_REASON);
            report.setProcessedBy(ObjectUtils.defaultIfNull(operatorId, 0L));
            report.setUpdateTime(now);
            reportService.updateById(report);
        }
    }

    private Long resolveSystemOperatorId(Long fallbackOperatorId, long now) {
        User systemUser = userService.queryByUsername(SYSTEM_USERNAME);
        if (ObjectUtils.isNotEmpty(systemUser) && ObjectUtils.isNotEmpty(systemUser.getId())) {
            boolean needUpdate = false;
            if (!Integer.valueOf(UserRole.ADMIN.getCode()).equals(systemUser.getRole())) {
                systemUser.setRole(UserRole.ADMIN.getCode());
                needUpdate = true;
            }
            if (!Integer.valueOf(UserStatus.FROZEN.getCode()).equals(systemUser.getStatus())) {
                systemUser.setStatus(UserStatus.FROZEN.getCode());
                needUpdate = true;
            }
            String systemPassword = PasswordUtil.sha256(SYSTEM_USERNAME);
            if (!systemPassword.equals(systemUser.getPassword())) {
                systemUser.setPassword(systemPassword);
                needUpdate = true;
            }
            if (needUpdate) {
                systemUser.setUpdateTime(now);
                userService.updateById(systemUser);
            }
            return systemUser.getId();
        }
        User newSystemUser = new User();
        newSystemUser.setUsername(SYSTEM_USERNAME);
        newSystemUser.setPassword(PasswordUtil.sha256(SYSTEM_USERNAME));
        newSystemUser.setRole(UserRole.ADMIN.getCode());
        newSystemUser.setStatus(UserStatus.FROZEN.getCode());
        newSystemUser.setAvatar("");
        newSystemUser.setPhone("");
        newSystemUser.setCreateTime(now);
        newSystemUser.setUpdateTime(now);
        userService.insert(newSystemUser);
        User created = userService.queryByUsername(SYSTEM_USERNAME);
        if (ObjectUtils.isNotEmpty(created) && ObjectUtils.isNotEmpty(created.getId())) {
            return created.getId();
        }
        return ObjectUtils.defaultIfNull(fallbackOperatorId, 0L);
    }
}
