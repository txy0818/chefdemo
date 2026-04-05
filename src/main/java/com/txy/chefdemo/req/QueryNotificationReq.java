package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryNotificationReq {
    private Boolean unreadOnly;
    private Long page = 0L;
    private Long size = 0L;
}
