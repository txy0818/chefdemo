package com.txy.chefdemo.service.impl;

import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.bo.UserSearchBo;
import com.txy.chefdemo.mapper.UserMapper;
import com.txy.chefdemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public Long insert(User user) {
        return userMapper.insert(user);
    }

    @Override
    public int updateById(User user) {
        return userMapper.updateById(user);
    }

    @Override
    public Long upsert(User user) {
        return userMapper.upsert(user);
    }

    @Override
    public User queryById(Long id) {
        return userMapper.queryById(id);
    }

    @Override
    public User queryByUsername(String username) {
        return userMapper.queryByUsername(username);
    }

    @Override
    public List<User> queryUserListByCondition(UserSearchBo userSearchBo) {
        return userMapper.queryUserListByCondition(userSearchBo);
    }

    @Override
    public int queryUserListCnt(UserSearchBo userSearchBo) {
        return userMapper.queryUserListCnt(userSearchBo);
    }

    @Override
    public List<User> queryByIdList(List<Long> chefIdList) {
        return userMapper.queryByIdList(chefIdList);
    }
}
