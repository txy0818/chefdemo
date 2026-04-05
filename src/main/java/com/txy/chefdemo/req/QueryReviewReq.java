package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryReviewReq {
    private Integer auditStatus = 0;
    private Long page = 0L;
    private Long size = 10L;
}
