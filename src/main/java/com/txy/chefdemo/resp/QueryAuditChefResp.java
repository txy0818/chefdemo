package com.txy.chefdemo.resp;

import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.dto.ChefProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-04-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryAuditChefResp {
    private BaseResp baseResp;
    private List<ChefProfileDTO> data;
    private Integer total;
}
