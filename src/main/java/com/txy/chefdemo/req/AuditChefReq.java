package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditChefReq {
    private Long chefUserId = 0L;
    private Integer auditStatus = 0;
    private String reason = "";
}
