package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.NotificationRecord;
import com.txy.chefdemo.domain.dto.UserWalletDTO;
import com.txy.chefdemo.req.CreateReportReq;
import com.txy.chefdemo.req.CreateReviewReq;
import com.txy.chefdemo.req.DeleteReviewReq;
import com.txy.chefdemo.req.QueryNotificationReq;
import com.txy.chefdemo.req.QueryWalletRecordReq;
import com.txy.chefdemo.req.ReadNotificationReq;
import com.txy.chefdemo.req.RechargeWalletReq;
import com.txy.chefdemo.resp.ListResp;
import com.txy.chefdemo.resp.QueryWalletRecordResp;

public interface UserInteractionService {

    UserWalletDTO getWalletBalance(Long currentUserId);

    QueryWalletRecordResp queryWalletRecords(Long currentUserId, QueryWalletRecordReq req);

    UserWalletDTO rechargeWallet(Long currentUserId, RechargeWalletReq req);

    void createReview(Long currentUserId, CreateReviewReq req);

    void deleteReview(Long currentUserId, DeleteReviewReq req);

    void createReport(Long currentUserId, CreateReportReq req);

    ListResp<NotificationRecord> notificationList(Long currentUserId, QueryNotificationReq req);

    void readNotification(Long currentUserId, ReadNotificationReq req);
}
