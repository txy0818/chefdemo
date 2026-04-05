package com.txy.chefdemo.resp;

import com.txy.chefdemo.domain.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author tianxinyu
 * @Create 2026-04-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryInfoResp {
    private BaseResp baseResp;
    private UserDTO data;
}
