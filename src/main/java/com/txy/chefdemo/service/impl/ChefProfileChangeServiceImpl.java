package com.txy.chefdemo.service.impl;

import com.txy.chefdemo.domain.ChefProfileChange;
import com.txy.chefdemo.mapper.ChefProfileChangeMapper;
import com.txy.chefdemo.service.ChefProfileChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChefProfileChangeServiceImpl implements ChefProfileChangeService {

    @Autowired
    private ChefProfileChangeMapper chefProfileChangeMapper;

    @Override
    public Long insert(ChefProfileChange change) {
        return chefProfileChangeMapper.insert(change);
    }

    @Override
    public int updateById(ChefProfileChange change) {
        return chefProfileChangeMapper.updateById(change);
    }

    @Override
    public ChefProfileChange queryByUserId(Long userId) {
        return chefProfileChangeMapper.queryByUserId(userId);
    }
}
