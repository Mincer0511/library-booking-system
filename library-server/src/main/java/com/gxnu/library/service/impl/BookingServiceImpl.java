package com.gxnu.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnu.library.common.constant.ConfigKey;
import com.gxnu.library.common.constant.OrderStatus;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.ResultCode;
import com.gxnu.library.dto.BookingCreateDTO;
import com.gxnu.library.dto.BookingExtendDTO;
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
import com.gxnu.library.service.CreditService;
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
    private final CreditService creditService;

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
    @Transactional(rollbackFor = Exception.class)
    public void checkin(Long orderId) {
        BookingOrder order = getOwnOrder(orderId);
        if (order.getStatus() != OrderStatus.WAIT_CHECKIN) {
            throw new BusinessException(ResultCode.BOOKING_STATE_ERROR, "当前状态不可签到");
        }
        int lateMinutes = configService.getInt(ConfigKey.CHECKIN_LATE_MINUTES, 30);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDateTime.of(order.getBookingDate(), order.getStartTime());
        if (now.isBefore(startDateTime)) {
            throw new BusinessException("未到签到时间，可于 " + order.getStartTime().format(TIME_FMT) + " 起签到");
        }
        if (now.isAfter(startDateTime.plusMinutes(lateMinutes))) {
            throw new BusinessException(ResultCode.BOOKING_STATE_ERROR, "已超过签到时限，订单将被判定为爽约");
        }
        order.setStatus(OrderStatus.IN_USE);
        order.setCheckinTime(now);
        bookingMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkout(Long orderId) {
        BookingOrder order = getOwnOrder(orderId);
        if (order.getStatus() != OrderStatus.IN_USE) {
            throw new BusinessException(ResultCode.BOOKING_STATE_ERROR, "当前状态不可签退");
        }
        order.setStatus(OrderStatus.FINISHED);
        order.setCheckoutTime(LocalDateTime.now());
        bookingMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void extend(Long orderId, BookingExtendDTO dto) {
        BookingOrder order = getOwnOrder(orderId);
        if (order.getStatus() != OrderStatus.IN_USE) {
            throw new BusinessException(ResultCode.BOOKING_STATE_ERROR, "当前状态不可续时");
        }
        LocalTime newEnd = LocalTime.parse(dto.getNewEndTime(), TIME_FMT);
        if (!newEnd.isAfter(order.getEndTime())) {
            throw new BusinessException("续时后的结束时间须晚于当前结束时间");
        }
        // 续时后结束时间不得晚于当前时刻（防止穿越），也不得超过自习室关闭时间
        if (LocalDateTime.of(order.getBookingDate(), newEnd).isBefore(LocalDateTime.now())) {
            throw new BusinessException("新的结束时间已过，请重新选择");
        }
        StudyRoom room = roomMapper.selectById(order.getRoomId());
        if (room != null && newEnd.isAfter(room.getCloseTime())) {
            throw new BusinessException("续时后结束时间超出自习室关闭时间 " + room.getCloseTime());
        }
        // 总时长（开始时间 ~ 新结束时间）不得超单次上限
        int maxHours = configService.getInt(ConfigKey.BOOKING_MAX_HOURS, 4);
        long totalMinutes = Duration.between(order.getStartTime(), newEnd).toMinutes();
        if (totalMinutes > maxHours * 60L) {
            throw new BusinessException("单次使用总时长最长" + maxHours + "小时，无法继续续时");
        }
        // 延长时段 [原end, 新end) 座位须空闲（排除本订单）
        int conflict = bookingMapper.countSeatConflictExclude(
                order.getSeatId(), order.getBookingDate(), order.getEndTime(), newEnd, orderId);
        if (conflict > 0) {
            throw new BusinessException(ResultCode.BOOKING_CONFLICT, "续时时段该座位已被他人预约");
        }
        order.setEndTime(newEnd);
        bookingMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markNoShow(Long orderId) {
        BookingOrder order = bookingMapper.selectById(orderId);
        if (order == null || order.getStatus() != OrderStatus.WAIT_CHECKIN) {
            return;
        }
        order.setStatus(OrderStatus.NO_SHOW);
        bookingMapper.updateById(order);

        int deductScore = configService.getInt(ConfigKey.CREDIT_VIOLATION_DEDUCT, 10);
        String desc = "预约 " + order.getBookingDate() + " "
                + order.getStartTime().format(TIME_FMT) + "-" + order.getEndTime().format(TIME_FMT)
                + " 未按时签到，系统判定爽约";
        creditService.deduct(order.getUserId(), orderId, "爽约", desc, deductScore, null);
    }

    @Override
    public PageResult<BookingVO> myPage(Integer status, long pageNum, long pageSize) {
        Long userId = UserContext.currentUserId();
        Page<BookingVO> page = new Page<>(pageNum, pageSize);
        Page<BookingVO> result = (Page<BookingVO>) bookingMapper.selectMyBookingPage(page, userId, status);
        result.getRecords().forEach(this::fillActions);
        return PageResult.of(result);
    }

    @Override
    public BookingVO myDetail(Long orderId) {
        Long userId = UserContext.currentUserId();
        BookingVO vo = bookingMapper.selectMyBookingDetail(orderId, userId);
        if (vo == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "预约记录不存在");
        }
        fillActions(vo);
        return vo;
    }

    /** 查询本人订单，不存在或非本人抛异常 */
    private BookingOrder getOwnOrder(Long orderId) {
        Long userId = UserContext.currentUserId();
        BookingOrder order = bookingMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "预约记录不存在");
        }
        return order;
    }

    /** 计算各操作按钮可用性：取消 / 签到 / 签退 / 续时 */
    private void fillActions(BookingVO vo) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDateTime.of(vo.getBookingDate(), vo.getStartTime());
        int lateMinutes = configService.getInt(ConfigKey.CHECKIN_LATE_MINUTES, 30);

        vo.setCanCancel(vo.getStatus() == OrderStatus.WAIT_CHECKIN && now.isBefore(startDateTime));
        vo.setCanCheckin(vo.getStatus() == OrderStatus.WAIT_CHECKIN
                && !now.isBefore(startDateTime)
                && !now.isAfter(startDateTime.plusMinutes(lateMinutes)));
        vo.setCanCheckout(vo.getStatus() == OrderStatus.IN_USE);
        vo.setCanExtend(vo.getStatus() == OrderStatus.IN_USE
                && now.isBefore(LocalDateTime.of(vo.getBookingDate(), vo.getEndTime())));
    }
}
