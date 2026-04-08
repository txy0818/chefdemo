package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveChefProfileReq {
    /** 真实姓名。 */
    private String realName;

    /** 身份证图片 URL 列表。 */
    private List<String> idCardImgs;

    /** 健康证图片 URL 列表。 */
    private List<String> healthCertImgs;

    /** 厨师证图片 URL 列表。 */
    private List<String> chefCertImgs;

    /** 擅长菜系编码列表。 */
    private List<Integer> cuisineType;

    /** 服务区域。 */
    private String serviceArea;

    /** 服务说明。 */
    private String serviceDesc;

    /** 每小时价格，金额以元为单位。 */
    private String price;

    /** 最少服务人数。 */
    private Integer minPeople;

    /** 最多服务人数。 */
    private Integer maxPeople;

    /** 年龄。 */
    private Integer age;

    /** 性别编码。 */
    private Integer gender;

    /** 从业年限。 */
    private Integer workYears;
}
