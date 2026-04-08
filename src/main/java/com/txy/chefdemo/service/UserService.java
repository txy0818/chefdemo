package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.bo.UserSearchBo;
import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface UserService {
    Long insert(User user);

    int updateById(User user);

    /**
     * 仅保留兜底使用，业务代码优先走 insert / updateById。
     */
    Long upsert(User user);

    User queryById(Long id);

    User queryByUsername(String username);

    List<User> queryUserListByCondition(UserSearchBo userSearchBo);

    int queryUserListCnt(UserSearchBo userSearchBo);

    List<User> queryByIdList(List<Long> chefIdList);
}
