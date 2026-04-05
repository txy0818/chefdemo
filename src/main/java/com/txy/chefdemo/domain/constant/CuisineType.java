package com.txy.chefdemo.domain.constant;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public enum CuisineType {
    SICHUAN(1, "川菜"),
    CANTONESE(2, "粤菜"),
    HUNAN(3, "湘菜"),
    SHANDONG(4, "鲁菜"),
    JIANGSU(5, "苏菜"),
    ZHEJIANG(6, "浙菜"),
    FUJIAN(7, "闽菜"),
    ANHUI(8, "徽菜"),
    BEIJING(9, "京菜"),
    SHANGHAI(10, "本帮菜"),
    NORTHEASTERN(11, "东北菜"),
    XINJIANG(12, "新疆菜"),
    YUNNAN(13, "云南菜"),
    GUANGXI(14, "桂菜"),
    HOTPOT(15, "火锅"),
    SEAFOOD(16, "海鲜"),
    VEGETARIAN(17, "素食"),
    WESTERN(18, "西餐"),
    JAPANESE(19, "日料"),
    KOREAN(20, "韩料");

    private final int code;
    private final String cuisineName;

    CuisineType(int code, String cuisineName) {
        this.code = code;
        this.cuisineName = cuisineName;
    }

    public int getCode() {
        return code;
    }

    public String getCuisineName() {
        return cuisineName;
    }

    // 根据code获取枚举
    public static CuisineType fromCode(int code) {
        for (CuisineType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    public static List<String> fromCodes(List<Integer> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return new ArrayList<>();
        }
        return codes.stream().map((code) -> {
            for (CuisineType type : values()) {
                if (type.code == code) {
                    return type.getCuisineName();
                }
            }
            return null;
        }).collect(Collectors.toList());
    }
}
