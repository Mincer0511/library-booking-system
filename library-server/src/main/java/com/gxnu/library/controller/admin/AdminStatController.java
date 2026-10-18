package com.gxnu.library.controller.admin;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
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
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 管理端数据统计：看板聚合 / 每日趋势 / 热门时段 / 利用率 / Excel 导出
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

    /** 导出利用率报表 Excel（xlsx） */
    @OperationLog(module = "数据统计", content = "导出利用率报表")
    @GetMapping("/export")
    public void export(@RequestParam String startDate, @RequestParam String endDate,
                       HttpServletResponse response) throws IOException {
        List<RoomUtilizationVO> data = adminStatService.roomUtilization(
                parseDate(startDate), parseDate(endDate), null, null);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        String fileName = URLEncoder.encode(
                "自习室利用率报表_" + startDate + "_" + endDate + ".xlsx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        try (ExcelWriter writer = ExcelUtil.getWriter(true)) {
            writer.writeHeadRow(List.of("日期", "自习室", "楼层", "座位总数", "预约时长(分钟)", "利用率(%)"));
            for (RoomUtilizationVO u : data) {
                writer.writeRow(List.of(
                        u.getDate().toString(),
                        u.getRoomName(),
                        u.getFloorNum() + "F",
                        u.getTotalSeats(),
                        u.getBookedMinutes(),
                        u.getUtilization()));
            }
            writer.flush(response.getOutputStream());
        }
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new BusinessException("日期格式错误，应为 yyyy-MM-dd");
        }
    }
}
