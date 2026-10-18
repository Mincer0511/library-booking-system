package com.gxnu.library.service;

import com.gxnu.library.vo.DailyCountVO;
import com.gxnu.library.vo.DashboardVO;
import com.gxnu.library.vo.HotHourVO;
import com.gxnu.library.vo.RoomUtilizationVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 管理端数据统计 Service
 */
public interface AdminStatService {

    /** 数据看板聚合：今日订单 / 进行中 / 爽约 / 用户数 / 周趋势 / 热门时段 */
    DashboardVO dashboard();

    /** 日期范围内每日订单数 */
    List<DailyCountVO> dailyCount(LocalDate startDate, LocalDate endDate);

    /** 日期范围内热门开始时段 */
    List<HotHourVO> hotHours(LocalDate startDate, LocalDate endDate);

    /** 自习室利用率（日 × 自习室，可按楼层/自习室过滤） */
    List<RoomUtilizationVO> roomUtilization(LocalDate startDate, LocalDate endDate,
                                            Long floorId, Long roomId);
}
