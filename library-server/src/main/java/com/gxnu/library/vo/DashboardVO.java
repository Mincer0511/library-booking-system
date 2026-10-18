package com.gxnu.library.vo;

import lombok.Data;

import java.util.List;

/**
 * 数据看板聚合 VO
 */
@Data
public class DashboardVO {

    /** 今日订单总数 */
    private Long todayBookings;

    /** 当前进行中订单数 */
    private Long inUseCount;

    /** 今日爽约数 */
    private Long todayNoShow;

    /** 注册用户总数 */
    private Long userCount;

    /** 自习室总数 */
    private Long roomCount;

    /** 近 7 天订单趋势 */
    private List<DailyCountVO> weekTrend;

    /** 近 30 天热门开始时段 */
    private List<HotHourVO> hotHours;
}
