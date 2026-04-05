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
    Long upsert(@Param("user") User user);
    User queryById(@Param("id") Long id);
    User queryByUsername(@Param("username") String username);
    List<User> queryUserListByCondition(@Param("search") UserSearchBo userSearchBo);

    int queryUserListCnt(@Param("search") UserSearchBo userSearchBo);

    List<User> queryByIdList(@Param("chefIdList") List<Long> chefIdList);
}
