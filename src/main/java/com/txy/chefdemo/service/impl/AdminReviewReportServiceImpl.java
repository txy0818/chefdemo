package com.txy.chefdemo.service.impl;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.domain.Report;
import com.txy.chefdemo.domain.Review;
import com.txy.chefdemo.domain.bo.ReportSearchBo;
import com.txy.chefdemo.domain.bo.ReviewSearchBo;
import com.txy.chefdemo.domain.constant.AuditStatus;
import com.txy.chefdemo.domain.constant.ReportStatus;
import com.txy.chefdemo.domain.constant.ReviewStatus;
import com.txy.chefdemo.domain.dto.ReportDTO;
import com.txy.chefdemo.domain.dto.ReviewDTO;
import com.txy.chefdemo.req.AuditReviewReq;
import com.txy.chefdemo.req.DeleteReviewReq;
import com.txy.chefdemo.req.HandleReportReq;
import com.txy.chefdemo.req.QueryReportReq;
import com.txy.chefdemo.req.QueryReviewReq;
import com.txy.chefdemo.resp.QueryReportResp;
import com.txy.chefdemo.resp.QueryReviewResp;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.AdminReviewReportService;
import com.txy.chefdemo.service.ReportService;
import com.txy.chefdemo.service.ReviewService;
import com.txy.chefdemo.service.UserService;
import com.txy.chefdemo.utils.DateUtils;
import com.txy.chefdemo.utils.DefaultValueUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminReviewReportServiceImpl implements AdminReviewReportService {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private UserService userService;

    @Override
    public QueryReviewResp reviewList(QueryReviewReq req) {
        ReviewSearchBo reviewSearchBo = buildReviewSearch(req);
        List<Review> reviews = reviewService.queryByCondition(reviewSearchBo);
        int cnt = reviewService.queryCnt(reviewSearchBo);
        List<ReviewDTO> reviewDTOS = buildReviewList(reviews);
        return new QueryReviewResp(BaseRespConstant.SUC, reviewDTOS, cnt);
    }

    @Override
    @Transactional
    public void auditReview(AuditReviewReq req) {
        Preconditions.checkArgument(req.getAuditStatus() != 0, "审核状态错误");
        if (AuditStatus.REJECTED.getCode() == req.getAuditStatus()) {
            Preconditions.checkArgument(StringUtils.isNotBlank(req.getReason()), "驳回原因不能为空");
        }
        String reason = StringUtils.trimToEmpty(req.getReason());
        ReviewSearchBo reviewSearchBo = new ReviewSearchBo();
        reviewSearchBo.setOrderId(req.getOrderId());
        List<Review> reviews = reviewService.queryByCondition(reviewSearchBo);
        Preconditions.checkArgument(!CollectionUtils.isEmpty(reviews), BaseRespConstant.FAIL.getDesc());

        Review review = reviews.get(0);
        review.setAuditStatus(req.getAuditStatus());
        if (AuditStatus.REJECTED.getCode() == req.getAuditStatus() && StringUtils.isNotBlank(reason)) {
            review.setAuditReason(reason);
        }
        review.setUpdateTime(System.currentTimeMillis());
        reviewService.updateById(review);
    }

    @Override
    @Transactional
    public void deleteReview(DeleteReviewReq req) {
        Preconditions.checkArgument(req.getOrderId() != 0, "订单ID错误");
        ReviewSearchBo reviewSearchBo = new ReviewSearchBo();
        reviewSearchBo.setOrderId(req.getOrderId());
        List<Review> reviews = reviewService.queryByCondition(reviewSearchBo);
        Preconditions.checkArgument(!CollectionUtils.isEmpty(reviews), BaseRespConstant.FAIL.getDesc());

        Review review = reviews.get(0);
        review.setStatus(ReviewStatus.DELETED.getCode());
        review.setUpdateTime(System.currentTimeMillis());
        reviewService.updateById(review);
    }

    @Override
    public QueryReportResp reportList(QueryReportReq req) {
        Preconditions.checkArgument(req.getStatus() != 0, "举报状态错误");
        ReportSearchBo reportSearchBo = buildReportSearch(req);
        List<Report> reports = reportService.queryByCondition(reportSearchBo);
        int cnt = reportService.queryCnt(reportSearchBo);
        List<ReportDTO> reportDTOS = buildReportList(reports);
        return new QueryReportResp(BaseRespConstant.SUC, reportDTOS, cnt);
    }

    @Override
    @Transactional
    public void handleReport(Long currentAdminId, HandleReportReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && ObjectUtils.isNotEmpty(req.getOrderId()), "举报不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(req.getProcessResult()), "处理结果不能为空");
        String processResult = req.getProcessResult().trim();

        ReportSearchBo reportSearchBo = new ReportSearchBo();
        reportSearchBo.setOrderId(req.getOrderId());
        List<Report> reports = reportService.queryByCondition(reportSearchBo);
        Preconditions.checkArgument(!CollectionUtils.isEmpty(reports), "举报不存在");

        Report report = reports.get(0);
        report.setStatus(req.getStatus());
        report.setProcessResult(processResult);
        report.setProcessedBy(currentAdminId);
        report.setUpdateTime(System.currentTimeMillis());
        reportService.updateById(report);
    }

    private ReviewSearchBo buildReviewSearch(QueryReviewReq req) {
        ReviewSearchBo reviewSearchBo = new ReviewSearchBo();
        if (req.getAuditStatus() != 0) {
            reviewSearchBo.setAuditStatus(req.getAuditStatus());
        }
        if (req.getSize() != 0L && req.getPage() != 0L) {
            reviewSearchBo.setOffset((req.getPage() - 1) * req.getSize());
            reviewSearchBo.setSize(req.getSize());
        } else {
            reviewSearchBo.setOffset(0L);
            reviewSearchBo.setSize(10L);
        }
        return reviewSearchBo;
    }

    private List<ReviewDTO> buildReviewList(List<Review> reviews) {
        return reviews.stream().map(review -> {
            ReviewDTO reviewDTO = new ReviewDTO();
            reviewDTO.setId(DefaultValueUtil.defaultLong(review.getId()));
            reviewDTO.setReservationOrderId(DefaultValueUtil.defaultLong(review.getReservationOrderId()));
            reviewDTO.setUserName(DefaultValueUtil.defaultString(userService.queryById(review.getUserId()).getUsername()));
            reviewDTO.setChefName(DefaultValueUtil.defaultString(userService.queryById(review.getChefId()).getUsername()));
            reviewDTO.setScore(
                    DefaultValueUtil.defaultString(BigDecimal.valueOf(review.getScore())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                            .toString())
            );
            reviewDTO.setContent(DefaultValueUtil.defaultString(review.getContent()));
            reviewDTO.setAuditStatus(DefaultValueUtil.defaultInteger(review.getAuditStatus()));
            reviewDTO.setAuditStatusDesc(DefaultValueUtil.defaultString(AuditStatus.getByCode(review.getAuditStatus()).getDesc()));
            reviewDTO.setAuditReason(StringUtils.isNotBlank(review.getAuditReason()) ? review.getAuditReason() : "-");
            reviewDTO.setCreateTime(DefaultValueUtil.defaultLong(review.getCreateTime()));
            reviewDTO.setCreateTimeDesc(DefaultValueUtil.defaultString(DateUtils.format(review.getCreateTime(), DateUtils.DATE_TIME_FORMAT)));
            return reviewDTO;
        }).collect(Collectors.toList());
    }

    private ReportSearchBo buildReportSearch(QueryReportReq req) {
        ReportSearchBo reportSearchBo = new ReportSearchBo();
        if (req.getStatus() != 0) {
            reportSearchBo.setStatus(req.getStatus());
        }
        if (req.getSize() != 0L && req.getPage() != 0L) {
            reportSearchBo.setOffset((req.getPage() - 1) * req.getSize());
            reportSearchBo.setSize(req.getSize());
        } else {
            reportSearchBo.setOffset(0L);
            reportSearchBo.setSize(10L);
        }
        return reportSearchBo;
    }

    private List<ReportDTO> buildReportList(List<Report> reports) {
        return reports.stream().map(report -> {
            ReportDTO reportDTO = new ReportDTO();
            reportDTO.setReservationOrderId(DefaultValueUtil.defaultLong(report.getReservationOrderId()));
            reportDTO.setReporterName(DefaultValueUtil.defaultString(userService.queryById(report.getReporterId()).getUsername()));
            reportDTO.setTargetUserName(DefaultValueUtil.defaultString(userService.queryById(report.getTargetUserId()).getUsername()));
            reportDTO.setReason(DefaultValueUtil.defaultString(report.getReason()));
            reportDTO.setProcessResult(StringUtils.isNotBlank(report.getProcessResult()) ? report.getProcessResult() : "-");
            reportDTO.setStatus(DefaultValueUtil.defaultInteger(report.getStatus()));
            reportDTO.setStatusDesc(DefaultValueUtil.defaultString(ReportStatus.getByCode(report.getStatus()).getDesc()));
            return reportDTO;
        }).collect(Collectors.toList());
    }
}
