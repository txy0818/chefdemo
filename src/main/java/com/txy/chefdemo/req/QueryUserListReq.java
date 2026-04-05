package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryUserListReq {
    private Long userId = 0L;
    private String username = "";
    private Integer status = 0;
    private Integer role = 0;
    private Long page = 0L;
    private Long size = 10L;
}
