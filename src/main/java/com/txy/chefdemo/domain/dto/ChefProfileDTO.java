package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChefProfileDTO {
    /** 厨师用户ID */
    private Long userId;
    /** 头像URL */
    private String avatar;
    /** 展示昵称 */
    private String displayName;
    /** 真实姓名 */
    private String realName;
    /** 身份证图片URL列表(JSON) */
    private List<String> idCardImgs;
    /** 健康证图片URL列表(JSON) */
    private List<String> healthCertImgs;
    /** 厨师证图片URL列表(JSON) */
    private List<String> chefCertImgs;
    /** 擅长菜系(枚举ID集合 1,2,3) */
    private List<String> cuisineType;
    /** 服务区域 */
    private String serviceArea;
    /** 服务说明/个人简介 */
    private String serviceDesc;
    /** 每小时价格(分) */
    private Long price;
    /** 最少服务人数 */
    private Integer minPeople;
    /** 最多服务人数 */
    private Integer maxPeople;
    /** 年龄 */
    private Integer age;
    /** 性别 1-男 2-女 */
    private String gender;
    /** 从业年限 */
    private Integer workYears;
    /** phone */
    private String phone;
    /** 审核状态 1-待审核 2-审核通过 3-审核未通过 */
    private String auditStatus;
    /** 评分*/
    private String score;
}
