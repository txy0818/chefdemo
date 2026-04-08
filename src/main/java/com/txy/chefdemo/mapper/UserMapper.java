package com.txy.chefdemo.mapper;

import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.bo.UserSearchBo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Mapper
public interface UserMapper {
    Long insert(@Param("user") User user);

    int updateById(@Param("user") User user);

    /**
     * 基于 INSERT ... ON DUPLICATE KEY UPDATE 的兜底写法。
     * 注意：MySQL 在唯一键冲突走 UPDATE 时，也可能推进 AUTO_INCREMENT，导致“吃号”。
     */
    Long upsert(@Param("user") User user);

    User queryById(@Param("id") Long id);

    User queryByUsername(@Param("username") String username);

    List<User> queryUserListByCondition(@Param("search") UserSearchBo userSearchBo);

    int queryUserListCnt(@Param("search") UserSearchBo userSearchBo);

    List<User> queryByIdList(@Param("chefIdList") List<Long> chefIdList);
}
