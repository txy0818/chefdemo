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
    /** 厨师用户 ID。 */
    private Long userId;

    /** 头像 URL。 */
    private String avatar;

    /** 展示昵称。 */
    private String displayName;

    /** 真实姓名。 */
    private String realName;

    /** 身份证图片 URL 列表。 */
    private List<String> idCardImgs;

    /** 健康证图片 URL 列表。 */
    private List<String> healthCertImgs;

    /** 厨师证图片 URL 列表。 */
    private List<String> chefCertImgs;

    /** 擅长菜系描述列表。 */
    private List<String> cuisineTypeDesc;

    /** 擅长菜系编码列表。 */
    private List<Integer> cuisineType;

    /** 服务区域。 */
    private String serviceArea;

    /** 服务说明或个人简介。 */
    private String serviceDesc;

    /** 每小时价格，单位为分。 */
    private Long price;

    /** 每小时价格描述，金额以元为单位。 */
    private String priceDesc;

    /** 最少服务人数。 */
    private Integer minPeople;

    /** 最多服务人数。 */
    private Integer maxPeople;

    /** 年龄。 */
    private Integer age;

    /** 性别编码。 */
    private Integer gender;

    /** 性别描述。 */
    private String genderDesc;

    /** 从业年限。 */
    private Integer workYears;

    /** 联系手机号。 */
    private String phone;

    /** 审核状态编码。 */
    private Integer auditStatus;

    /** 审核状态描述。 */
    private String auditStatusDesc;

    /** 评分描述。 */
    private String score;
}
