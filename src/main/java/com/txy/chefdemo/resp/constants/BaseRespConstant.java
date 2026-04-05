package com.txy.chefdemo.resp.constants;


import com.txy.chefdemo.resp.BaseResp;

/**
 * @Author tianxinyu
 * @Create 2026-01-08
 */
public class BaseRespConstant {
    public static final BaseResp SUC = new BaseResp(1, "成功");
    public static final BaseResp USER_EXIST = new BaseResp(2, "用户已经存在");
    public static final BaseResp USER_NOT_EXIST = new BaseResp(3, "用户不存在");
    public static final BaseResp PASSWORD_ERROR = new BaseResp(4, "密码错误");
    public static final BaseResp STATUS_ERROR = new BaseResp(5, "当前状态不允许该操作");
    public static final BaseResp BALANCE_NOT_ENOUGH = new BaseResp(6, "余额不足");
    public static final BaseResp TIME_NOT_AVAILABLE = new BaseResp(7, "时间段不可预约");
    public static final BaseResp REVIEW_EXIST = new BaseResp(8, "订单已经评价过");
    public static final BaseResp AUDIT_NOT_PASS = new BaseResp(9, "厨师审核尚未通过");

    // 一般失败
    public static final BaseResp FAIL = new BaseResp(10, "失败");

    // 无权限
    public static final BaseResp FORBIDDEN = new BaseResp(11, "无权限");

    public static BaseResp failUnknown(String s) {
        return new BaseResp(100, s);
    }
}
