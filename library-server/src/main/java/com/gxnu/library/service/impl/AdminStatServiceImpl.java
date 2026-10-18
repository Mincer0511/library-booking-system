package com.gxnu.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxnu.library.common.constant.OrderStatus;
import com.gxnu.library.entity.BookingOrder;
import com.gxnu.library.entity.StudyRoom;
import com.gxnu.library.entity.SysUser;
import com.gxnu.library.mapper.BookingOrderMapper;
import com.gxnu.library.mapper.StudyRoomMapper;
import com.gxnu.library.mapper.SysUserMapper;
import com.gxnu.library.service.AdminStatService;
import com.gxnu.library.vo.DailyCountVO;
import com.gxnu.library.vo.DashboardVO;
import com.gxnu.library.vo.HotHourVO;
import com.gxnu.library.vo.RoomUtilizationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 管理端数据统计实现
 */
@Service
@RequiredArgsConstructor
public class AdminStatServiceImpl implements AdminStatService {

    private final BookingOrderMapper bookingMapper;
    private final StudyRoomMapper roomMapper;
    private final SysUserMapper userMapper;

    @Override
    public DashboardVO dashboard() {
        LocalDate today = LocalDate.now();
        DashboardVO vo = new DashboardVO();
        vo.setTodayBookings(bookingMapper.selectCount(
                new LambdaQueryWrapper<BookingOrder>().eq(BookingOrder::getBookingDate, today)));
        vo.setInUseCount(bookingMapper.selectCount(
                new LambdaQueryWrapper<BookingOrder>().eq(BookingOrder::getStatus, OrderStatus.IN_USE)));
        vo.setTodayNoShow(bookingMapper.selectCount(new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getBookingDate, today)
                .eq(BookingOrder::getStatus, OrderStatus.NO_SHOW)));
        vo.setUserCount(userMapper.selectCount(null));
        vo.setRoomCount(roomMapper.selectCount(null));
        vo.setWeekTrend(bookingMapper.statDailyCount(today.minusDays(6), today));
        vo.setHotHours(bookingMapper.statHotHours(today.minusDays(29), today));
        return vo;
    }

    @Override
    public List<DailyCountVO> dailyCount(LocalDate startDate, LocalDate endDate) {
        return bookingMapper.statDailyCount(startDate, endDate);
    }

    @Override
    public List<HotHourVO> hotHours(LocalDate startDate, LocalDate endDate) {
        return bookingMapper.statHotHours(startDate, endDate);
    }

    @Override
    public List<RoomUtilizationVO> roomUtilization(LocalDate startDate, LocalDate endDate,
                                                   Long floorId, Long roomId) {
        return bookingMapper.statRoomUtilization(startDate, endDate, floorId, roomId);
    }
}
