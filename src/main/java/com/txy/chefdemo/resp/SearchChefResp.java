package com.txy.chefdemo.resp;

import com.txy.chefdemo.domain.dto.ChefCardDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-04-02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchChefResp {
    private BaseResp baseResp;
    private List<ChefCardDTO> data;
    private int total;
}
