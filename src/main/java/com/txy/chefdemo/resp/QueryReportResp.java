package com.txy.chefdemo.resp;

import com.txy.chefdemo.domain.dto.ReportDTO;
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
public class QueryReportResp {
    private BaseResp baseResp;
    private List<ReportDTO> data;
    private int total;
}
