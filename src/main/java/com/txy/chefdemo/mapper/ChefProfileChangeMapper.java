package com.txy.chefdemo.mapper;

import com.txy.chefdemo.domain.ChefProfileChange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChefProfileChangeMapper {
    Long insert(@Param("change") ChefProfileChange change);

    int updateById(@Param("change") ChefProfileChange change);

    ChefProfileChange queryByUserId(@Param("userId") Long userId);
}
