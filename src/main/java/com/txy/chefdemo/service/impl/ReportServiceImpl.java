package com.txy.chefdemo.service.impl;

import com.txy.chefdemo.domain.Report;
import com.txy.chefdemo.domain.bo.ReportSearchBo;
import com.txy.chefdemo.mapper.ReportMapper;
import com.txy.chefdemo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private ReportMapper reportMapper;

    @Override
    public List<Report> queryByCondition(ReportSearchBo reportSearchBo) {
        return reportMapper.queryByCondition(reportSearchBo);
    }

    @Override
    public int queryCnt(ReportSearchBo reportSearchBo) {
        return reportMapper.queryCnt(reportSearchBo);
    }

    @Override
    public int updateById(Report report) {
        return reportMapper.updateById(report);
    }

    @Override
    public Long insert(Report report) {
        return reportMapper.insert(report);
    }
}
