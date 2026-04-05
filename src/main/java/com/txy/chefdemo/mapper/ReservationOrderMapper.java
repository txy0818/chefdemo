package com.txy.chefdemo.mapper;

import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.bo.ReservationOrderSearchBo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Mapper
public interface ReservationOrderMapper {
    Long insert(@Param("order") ReservationOrder order);

    Long updateById(@Param("order") ReservationOrder order);

    List<ReservationOrder> queryByCondition(@Param("searchBo") ReservationOrderSearchBo searchBo);

    int queryCnt(@Param("searchBo") ReservationOrderSearchBo searchBo);
}
