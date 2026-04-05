package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatisticsDTO {

    /** 总订单数 */
    private Long total;

    /** 各状态统计 */
    private Long pendingPayment;
    private Long pendingAccept;
    private Long accepted;
    private Long completed;
    private Long cancelled;
    private Long rejected;

    /** 今日饼图 */
    private List<PieItem> todayPie;

    /** 一周趋势 */
    private List<String> dates;
    private List<Long> counts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PieItem {
        private String name;
        private Long value;
    }
}
