package com.txy.chefdemo.service.impl;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.domain.NotificationRecord;
import com.txy.chefdemo.domain.Report;
import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.Review;
import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.Wallet;
import com.txy.chefdemo.domain.WalletRecord;
import com.txy.chefdemo.domain.bo.ReservationOrderSearchBo;
import com.txy.chefdemo.domain.bo.ReviewSearchBo;
import com.txy.chefdemo.domain.constant.NotificationReadStatus;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.domain.constant.ReportStatus;
import com.txy.chefdemo.domain.constant.ReviewAuditStatus;
import com.txy.chefdemo.domain.constant.ReviewStatus;
import com.txy.chefdemo.domain.constant.WalletRecordType;
import com.txy.chefdemo.domain.dto.UserWalletDTO;
import com.txy.chefdemo.domain.dto.WalletRecordDTO;
import com.txy.chefdemo.exp.BusinessException;
import com.txy.chefdemo.req.CreateReportReq;
import com.txy.chefdemo.req.CreateReviewReq;
import com.txy.chefdemo.req.DeleteReviewReq;
import com.txy.chefdemo.req.QueryNotificationReq;
import com.txy.chefdemo.req.QueryWalletRecordReq;
import com.txy.chefdemo.req.ReadNotificationReq;
import com.txy.chefdemo.req.RechargeWalletReq;
import com.txy.chefdemo.resp.ListResp;
import com.txy.chefdemo.resp.QueryWalletRecordResp;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.NotificationRecordService;
import com.txy.chefdemo.service.ReportService;
import com.txy.chefdemo.service.ReservationOrderService;
import com.txy.chefdemo.service.ReviewService;
import com.txy.chefdemo.service.UserInteractionService;
import com.txy.chefdemo.service.UserService;
import com.txy.chefdemo.service.WalletRecordService;
import com.txy.chefdemo.service.WalletService;
import com.txy.chefdemo.utils.DateUtils;
import com.txy.chefdemo.utils.DefaultValueUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserInteractionServiceImpl implements UserInteractionService {

    @Autowired
    private UserService userService;
    @Autowired
    private ReservationOrderService reservationOrderService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private NotificationRecordService notificationRecordService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletRecordService walletRecordService;

    @Override
    public UserWalletDTO getWalletBalance(Long currentUserId) {
        User user = userService.queryById(currentUserId);
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(user), "用户不存在");
        Wallet wallet = ensureWallet(currentUserId);
        return buildUserWalletDTO(wallet);
    }

    @Override
    public QueryWalletRecordResp queryWalletRecords(Long currentUserId, QueryWalletRecordReq req) {
        User user = userService.queryById(currentUserId);
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(user), "用户不存在");

        long page = ObjectUtils.isEmpty(req.getPage()) || req.getPage() <= 0 ? 1L : req.getPage();
        long size = ObjectUtils.isEmpty(req.getSize()) || req.getSize() <= 0 ? 10L : req.getSize();
        long offset = (page - 1) * size;
        List<WalletRecordDTO> records = walletRecordService.queryByUserIdPage(currentUserId, offset, size)
                .stream()
                .map(this::buildWalletRecordDTO)
                .collect(Collectors.toList());
        int total = walletRecordService.queryByUserIdCount(currentUserId);
        return new QueryWalletRecordResp(BaseRespConstant.SUC, records, total);
    }

    @Override
    @Transactional
    public UserWalletDTO rechargeWallet(Long currentUserId, RechargeWalletReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req.getAmount()) && req.getAmount() > 0L, "充值金额不能为空");
        User user = userService.queryById(currentUserId);
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(user), "用户不存在");

        long now = System.currentTimeMillis();
        Wallet wallet = ensureWallet(currentUserId);
        wallet.setBalance(ObjectUtils.defaultIfNull(wallet.getBalance(), 0L) + req.getAmount());
        wallet.setUpdateTime(now);
        walletService.updateById(wallet);

        WalletRecord walletRecord = new WalletRecord();
        walletRecord.setUserId(currentUserId);
        walletRecord.setReservationOrderId(0L);
        walletRecord.setAmount(req.getAmount());
        walletRecord.setType(WalletRecordType.RECHARGE.getCode());
        walletRecord.setCreateTime(now);
        walletRecord.setUpdateTime(now);
        walletRecordService.insert(walletRecord);

        return buildUserWalletDTO(walletService.queryByUserId(currentUserId));
    }

    @Override
    @Transactional
    public void createReview(Long currentUserId, CreateReviewReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && ObjectUtils.isNotEmpty(req.getOrderId()), "订单不能为空");
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req.getScore()) && req.getScore() >= 1 && req.getScore() <= 5, "评分非法");

        ReservationOrder order = requireOwnedCompletedOrder(currentUserId, req.getOrderId(), "无权限", BaseRespConstant.STATUS_ERROR.getDesc());
        ReviewSearchBo reviewSearchBo = new ReviewSearchBo();
        reviewSearchBo.setOrderId(req.getOrderId());
        List<Review> existingReviews = reviewService.queryByCondition(reviewSearchBo);
        if (!CollectionUtils.isEmpty(existingReviews)) {
            throw new BusinessException(BaseRespConstant.REVIEW_EXIST.getDesc());
        }

        long now = System.currentTimeMillis();
        Review review = new Review();
        review.setReservationOrderId(req.getOrderId());
        review.setUserId(currentUserId);
        review.setChefId(order.getChefId());
        review.setScore(BigDecimal.valueOf(req.getScore()).multiply(BigDecimal.valueOf(100)).longValue());
        review.setContent(req.getContent());
        review.setAuditStatus(ReviewAuditStatus.PENDING.getCode());
        review.setStatus(ReviewStatus.NORMAL.getCode());
        review.setCreateTime(now);
        review.setUpdateTime(now);
        reviewService.insert(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long currentUserId, DeleteReviewReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req.getOrderId()), "订单不能为空");
        ReviewSearchBo reviewSearchBo = new ReviewSearchBo();
        reviewSearchBo.setOrderId(req.getOrderId());
        List<Review> reviews = reviewService.queryByCondition(reviewSearchBo);
        if (CollectionUtils.isEmpty(reviews)) {
            throw new BusinessException("评论不存在");
        }

        Review review = reviews.get(0);
        if (!Objects.equals(review.getUserId(), currentUserId)) {
            throw new BusinessException("无权限");
        }

        review.setStatus(ReviewStatus.DELETED.getCode());
        review.setUpdateTime(System.currentTimeMillis());
        reviewService.updateById(review);
    }

    @Override
    @Transactional
    public void createReport(Long currentUserId, CreateReportReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && ObjectUtils.isNotEmpty(req.getOrderId()), "订单不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(req.getReason()), "举报理由不能为空");
        String reason = req.getReason().trim();
        ReservationOrder order = requireOwnedCompletedOrder(
                currentUserId,
                req.getOrderId(),
                "无权限",
                "仅" + OrderStatus.COMPLETED.getDesc() + "订单可举报"
        );
        ReportSearchBo reportSearchBo = new ReportSearchBo();
        reportSearchBo.setOrderId(req.getOrderId());
        reportSearchBo.setStatus(ReportStatus.PENDING.getCode());
        List<Report> existingReports = reportService.queryByCondition(reportSearchBo);
        if (!CollectionUtils.isEmpty(existingReports)) {
            throw new BusinessException("该订单已有待处理举报，请勿重复提交");
        }

        long now = System.currentTimeMillis();
        Report report = new Report();
        report.setReservationOrderId(req.getOrderId());
        report.setReporterId(currentUserId);
        report.setTargetUserId(order.getChefId());
        report.setReason(reason);
        report.setStatus(ReportStatus.PENDING.getCode());
        report.setCreateTime(now);
        report.setUpdateTime(now);
        reportService.insert(report);
    }

    @Override
    public ListResp<NotificationRecord> notificationList(Long currentUserId, QueryNotificationReq req) {
        long page = ObjectUtils.isEmpty(req.getPage()) || req.getPage() <= 0 ? 1L : req.getPage();
        long size = ObjectUtils.isEmpty(req.getSize()) || req.getSize() <= 0 ? 10L : req.getSize();
        long offset = (page - 1) * size;
        List<NotificationRecord> list = notificationRecordService.queryPageByUserId(
                currentUserId, req.getUnreadOnly(), offset, size);
        int total = notificationRecordService.queryCountByUserId(currentUserId, req.getUnreadOnly());
        return new ListResp<>(BaseRespConstant.SUC, list, total);
    }

    @Override
    @Transactional
    public void readNotification(Long currentUserId, ReadNotificationReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && ObjectUtils.isNotEmpty(req.getNotificationId()), "通知不能为空");
        NotificationRecord record = notificationRecordService.queryById(req.getNotificationId());
        if (ObjectUtils.isEmpty(record)) {
            throw new BusinessException("通知不存在");
        }
        if (!Objects.equals(record.getUserId(), currentUserId)) {
            throw new BusinessException("无权限");
        }
        record.setReadStatus(NotificationReadStatus.READ.getCode());
        record.setUpdateTime(System.currentTimeMillis());
        notificationRecordService.updateById(record);
    }

    private ReservationOrder requireOwnedCompletedOrder(Long currentUserId, Long orderId, String noPermissionMsg, String invalidStatusMsg) {
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setOrderId(orderId);
        List<ReservationOrder> orders = reservationOrderService.queryByCondition(searchBo);
        if (CollectionUtils.isEmpty(orders)) {
            throw new BusinessException("订单不存在");
        }
        ReservationOrder order = orders.get(0);
        if (!Objects.equals(order.getUserId(), currentUserId)) {
            throw new BusinessException(noPermissionMsg);
        }
        if (!Objects.equals(order.getStatus(), OrderStatus.COMPLETED.getCode())) {
            throw new BusinessException(invalidStatusMsg);
        }
        return order;
    }

    private Wallet ensureWallet(Long userId) {
        Wallet wallet = walletService.queryByUserId(userId);
        if (ObjectUtils.isNotEmpty(wallet)) {
            return wallet;
        }
        long now = System.currentTimeMillis();
        Wallet newWallet = new Wallet();
        newWallet.setUserId(userId);
        newWallet.setBalance(0L);
        newWallet.setCreateTime(now);
        newWallet.setUpdateTime(now);
        walletService.insert(newWallet);
        return walletService.queryByUserId(userId);
    }

    private UserWalletDTO buildUserWalletDTO(Wallet wallet) {
        return new UserWalletDTO(
                ObjectUtils.defaultIfNull(wallet.getId(), 0L),
                ObjectUtils.defaultIfNull(wallet.getUserId(), 0L),
                ObjectUtils.defaultIfNull(wallet.getBalance(), 0L),
                DefaultValueUtil.formatYuan(wallet.getBalance())
        );
    }

    private WalletRecordDTO buildWalletRecordDTO(WalletRecord record) {
        WalletRecordType walletRecordType = null;
        for (WalletRecordType type : WalletRecordType.values()) {
            if (type.getCode() == record.getType()) {
                walletRecordType = type;
                break;
            }
        }
        return new WalletRecordDTO(
                ObjectUtils.defaultIfNull(record.getId(), 0L),
                ObjectUtils.defaultIfNull(record.getReservationOrderId(), 0L),
                ObjectUtils.defaultIfNull(record.getAmount(), 0L),
                DefaultValueUtil.formatYuan(record.getAmount()),
                record.getType(),
                ObjectUtils.isNotEmpty(walletRecordType) ? walletRecordType.getDesc() : "-",
                ObjectUtils.defaultIfNull(record.getCreateTime(), 0L),
                DefaultValueUtil.defaultString(DateUtils.format(record.getCreateTime(), DateUtils.DATE_TIME_FORMAT))
        );
    }
}
