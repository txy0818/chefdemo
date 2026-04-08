package com.txy.chefdemo.service;

import com.txy.chefdemo.req.AuditChefReq;
import com.txy.chefdemo.req.SendChefMessageReq;
import com.txy.chefdemo.req.UpdateUserStatusReq;

public interface AdminOperationService {

    void updateUserStatus(Long currentAdminId, UpdateUserStatusReq req);

    void auditChef(Long currentAdminId, AuditChefReq req);

    void sendChefMessage(Long currentAdminId, SendChefMessageReq req);
}
