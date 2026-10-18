package com.gxnu.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnu.library.common.constant.ConfigKey;
import com.gxnu.library.common.constant.OrderStatus;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.ResultCode;
import com.gxnu.library.dto.BookingCreateDTO;
import com.gxnu.library.entity.BookingOrder;
import com.gxnu.library.entity.StudyRoom;
import com.gxnu.library.entity.StudySeat;
import com.gxnu.library.entity.SysUser;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.BookingOrderMapper;
import com.gxnu.library.mapper.StudyRoomMapper;
import com.gxnu.library.mapper.StudySeatMapper;
import com.gxnu.library.mapper.SysUserMapper;
import com.gxnu.library.service.BookingService;
import com.gxnu.library.service.ConfigService;
import com.gxnu.library.utils.UserContext;
import com.gxnu.library.vo.BookingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 预约 Service 实现（核心规则：冲突校验、每日限约、时长限制）
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int SLOT_MINUTES = 30;

    private final BookingOrderMapper bookingMapper;
    private final StudySeatMapper seatMapper;
    private final StudyRoomMapper roomMapper;
    private final SysUserMapper userMapper;
    private final ConfigService configService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(BookingCreateDTO dto) {
        Long userId = UserContext.currentUserId();
        SysUser user = userMapper.selectById(userId);

        // 1. 账号状态与预约禁用校验
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        if (user.getIsDisabled() == 1) {
            String msg = "您处于预约禁用期";
            if (user.getDisableEndTime() != null) {
                msg += "，解禁时间：" + user.getDisableEndTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
            throw new BusinessException(ResultCode.USER_DISABLED, msg);
        }

        // 2. 座位与自习室校验
        StudySeat seat = seatMapper.selectById(dto.getSeatId());
        if (seat == null || seat.getStatus() != 1) {
            throw new BusinessException("座位不存在或不可用");
        }
        StudyRoom room = roomMapper.selectById(seat.getRoomId());
        if (room == null || room.getStatus() != 1) {
            throw new BusinessException("自习室不存在或未开放");
        }

        // 3. 时间规则校验
        LocalDate date = dto.getBookingDate();
        LocalTime start = LocalTime.parse(dto.getStartTime(), TIME_FMT);
        LocalTime end = LocalTime.parse(dto.getEndTime(), TIME_FMT);
        if (!end.isAfter(start)) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes < SLOT_MINUTES) {
            throw new BusinessException("单次预约最短30分钟");
        }
        int maxHours = configService.getInt(ConfigKey.BOOKING_MAX_HOURS, 4);
        if (minutes > maxHours * 60L) {
            throw new BusinessException("单次预约最长" + maxHours + "小时");
        }
        // 日期范围：今天 ~ 今天+可提前预约天数
        LocalDate today = LocalDate.now();
        int advanceDays = configService.getInt(ConfigKey.BOOKING_ADVANCE_DAYS, 1);
        if (date.isBefore(today) || date.isAfter(today.plusDays(advanceDays))) {
            throw new BusinessException(advanceDays == 0 ? "仅可预约当天" : "仅可预约今天或明天");
        }
        // 开始时间不得早于当前时间
        if (LocalDateTime.of(date, start).isBefore(LocalDateTime.now())) {
            throw new BusinessException("预约开始时间已过，请重新选择");
        }
        // 须在自习室开放时间内
        if (start.isBefore(room.getOpenTime()) || end.isAfter(room.getCloseTime())) {
            throw new BusinessException("预约时段须在开放时间 "
                    + room.getOpenTime() + " - " + room.getCloseTime() + " 内");
        }

        // 4. 每日预约单数限制（不含已取消）
        int maxPerDay = configService.getInt(ConfigKey.BOOKING_MAX_PER_DAY, 2);
        Long dayCount = bookingMapper.selectCount(new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getUserId, userId)
                .eq(BookingOrder::getBookingDate, date)
                .ne(BookingOrder::getStatus, OrderStatus.CANCELED));
        if (dayCount >= maxPerDay) {
            throw new BusinessException(ResultCode.BOOKING_LIMIT, "每日最多预约" + maxPerDay + "次");
        }

        // 5. 座位时段冲突校验（行锁防并发，区间相交即冲突）
        int seatConflict = bookingMapper.countSeatConflictForUpdate(seat.getSeatId(), date, start, end);
        if (seatConflict > 0) {
            throw new BusinessException(ResultCode.BOOKING_CONFLICT);
        }

        // 6. 同一用户同时段冲突校验（跨自习室也拦截）
        int userConflict = bookingMapper.countUserTimeConflict(userId, date, start, end);
        if (userConflict > 0) {
            throw new BusinessException("您在该时段已有其他预约");
        }

        // 7. 写入订单
        BookingOrder order = new BookingOrder();
        order.setUserId(userId);
        order.setSeatId(seat.getSeatId());
        order.setRoomId(room.getRoomId());
        order.setBookingDate(date);
        order.setStartTime(start);
        order.setEndTime(end);
        order.setStatus(OrderStatus.WAIT_CHECKIN);
        bookingMapper.insert(order);
        return order.getOrderId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long orderId) {
        Long userId = UserContext.currentUserId();
        BookingOrder order = bookingMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "预约记录不存在");
        }
        if (order.getStatus() != OrderStatus.WAIT_CHECKIN) {
            throw new BusinessException(ResultCode.BOOKING_STATE_ERROR, "当前状态不可取消");
        }
        if (!LocalDateTime.now().isBefore(LocalDateTime.of(order.getBookingDate(), order.getStartTime()))) {
            throw new BusinessException("预约已开始，无法取消");
        }
        order.setStatus(OrderStatus.CANCELED);
        bookingMapper.updateById(order);
    }

    @Override
    public PageResult<BookingVO> myPage(Integer status, long pageNum, long pageSize) {
        Long userId = UserContext.currentUserId();
        Page<BookingVO> page = new Page<>(pageNum, pageSize);
        Page<BookingVO> result = (Page<BookingVO>) bookingMapper.selectMyBookingPage(page, userId, status);
        result.getRecords().forEach(this::fillCanCancel);
        return PageResult.of(result);
    }

    @Override
    public BookingVO myDetail(Long orderId) {
        Long userId = UserContext.currentUserId();
        BookingVO vo = bookingMapper.selectMyBookingDetail(orderId, userId);
        if (vo == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "预约记录不存在");
        }
        fillCanCancel(vo);
        return vo;
    }

    /** 计算是否可取消：待签到且未到开始时间 */
    private void fillCanCancel(BookingVO vo) {
        boolean canCancel = vo.getStatus() == OrderStatus.WAIT_CHECKIN
                && LocalDateTime.now().isBefore(LocalDateTime.of(vo.getBookingDate(), vo.getStartTime()));
        vo.setCanCancel(canCancel);
    }
}
