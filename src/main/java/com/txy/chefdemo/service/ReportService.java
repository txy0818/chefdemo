package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.Report;
import com.txy.chefdemo.domain.bo.ReportSearchBo;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface ReportService {
    Long insert(Report report);

    int updateById(Report report);

    List<Report> queryByCondition(ReportSearchBo reportSearchBo);

    int queryCnt(ReportSearchBo reportSearchBo);
}
