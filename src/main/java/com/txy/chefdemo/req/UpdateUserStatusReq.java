package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusReq {
    private Long userId = 0L;
    private Long operatorId = 0L;
    private Integer status = 0;
    private String reason = "";
}
