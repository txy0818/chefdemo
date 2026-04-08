package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendChefMessageReq {
    private Long chefUserId = 0L;
    private String title = "";
    private String content = "";
}
