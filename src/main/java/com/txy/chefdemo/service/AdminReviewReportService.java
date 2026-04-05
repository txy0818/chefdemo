package com.txy.chefdemo.service;

import com.txy.chefdemo.req.AuditReviewReq;
import com.txy.chefdemo.req.DeleteReviewReq;
import com.txy.chefdemo.req.HandleReportReq;
import com.txy.chefdemo.req.QueryReportReq;
import com.txy.chefdemo.req.QueryReviewReq;
import com.txy.chefdemo.resp.QueryReportResp;
import com.txy.chefdemo.resp.QueryReviewResp;

public interface AdminReviewReportService {

    QueryReviewResp reviewList(QueryReviewReq req);

    void auditReview(AuditReviewReq req);

    void deleteReview(DeleteReviewReq req);

    QueryReportResp reportList(QueryReportReq req);

    void handleReport(Long currentAdminId, HandleReportReq req);
}
