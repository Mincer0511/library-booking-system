package com.gxnu.library.controller.admin;

import com.gxnu.library.annotation.OperationLog;
import com.gxnu.library.annotation.RequireRole;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.common.result.Result;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.service.AdminStatService;
import com.gxnu.library.vo.DailyCountVO;
import com.gxnu.library.vo.DashboardVO;
import com.gxnu.library.vo.HotHourVO;
import com.gxnu.library.vo.RoomUtilizationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 管理端数据统计：看板聚合 / 每日趋势 / 热门时段 / 利用率
 */
@RestController
@RequestMapping("/api/admin/stat")
@RequiredArgsConstructor
@RequireRole({RoleKey.LIBRARIAN, RoleKey.SUPER_ADMIN})
public class AdminStatController {

    private final AdminStatService adminStatService;

    /** 数据看板 */
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(adminStatService.dashboard());
    }

    /** 每日订单趋势（近 days 天） */
    @GetMapping("/daily")
    public Result<List<DailyCountVO>> daily(@RequestParam(defaultValue = "14") int days) {
        LocalDate end = LocalDate.now();
        return Result.success(adminStatService.dailyCount(end.minusDays(days - 1L), end));
    }

    /** 热门开始时段（近 days 天） */
    @GetMapping("/hot-hours")
    public Result<List<HotHourVO>> hotHours(@RequestParam(defaultValue = "30") int days) {
        LocalDate end = LocalDate.now();
        return Result.success(adminStatService.hotHours(end.minusDays(days - 1L), end));
    }

    /** 自习室利用率（日期范围 + 楼层/自习室过滤） */
    @GetMapping("/utilization")
    public Result<List<RoomUtilizationVO>> utilization(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) Long roomId) {
        return Result.success(adminStatService.roomUtilization(
                parseDate(startDate), parseDate(endDate), floorId, roomId));
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new BusinessException("日期格式错误，应为 yyyy-MM-dd");
        }
    }
}
