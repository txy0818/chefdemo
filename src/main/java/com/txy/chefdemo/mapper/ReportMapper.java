package com.txy.chefdemo.mapper;

import com.txy.chefdemo.domain.Report;
import com.txy.chefdemo.domain.bo.ReportSearchBo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Mapper
public interface ReportMapper {
    Long insert(@Param("report") Report report);

    int updateById(@Param("report") Report report);

    List<Report> queryByCondition(@Param("searchBo") ReportSearchBo reportSearchBo);

    int queryCnt(@Param("searchBo") ReportSearchBo reportSearchBo);
}
