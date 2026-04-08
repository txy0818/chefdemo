package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.ChefProfileChange;

public interface ChefProfileChangeService {
    Long insert(ChefProfileChange change);

    int updateById(ChefProfileChange change);

    ChefProfileChange queryByUserId(Long userId);
}
