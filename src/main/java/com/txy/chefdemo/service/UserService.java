package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.bo.UserSearchBo;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface UserService {
    Long upsert(User user);
    User queryById(Long id);
    User queryByUsername(String username);
    List<User> queryUserListByCondition(UserSearchBo userSearchBo);

    int queryUserListCnt(UserSearchBo userSearchBo);

    List<User> queryByIdList(List<Long> chefIdList);
}
