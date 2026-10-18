package com.gxnu.library.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnu.library.common.constant.OrderStatus;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.ResultCode;
import com.gxnu.library.dto.BookingTimeDTO;
import com.gxnu.library.entity.BookingOrder;
import com.gxnu.library.entity.StudyRoom;
import com.gxnu.library.entity.StudySeat;
import com.gxnu.library.entity.SysMessage;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.BookingOrderMapper;
import com.gxnu.library.mapper.StudyRoomMapper;
import com.gxnu.library.mapper.StudySeatMapper;
import com.gxnu.library.mapper.SysMessageMapper;
import com.gxnu.library.service.AdminBookingService;
import com.gxnu.library.vo.AdminBookingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 管理端订单运营实现
 */
@Service
@RequiredArgsConstructor
public class AdminBookingServiceImpl implements AdminBookingService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BookingOrderMapper bookingMapper;
    private final StudyRoomMapper roomMapper;
    private final StudySeatMapper seatMapper;
    private final SysMessageMapper messageMapper;

    @Override
    public PageResult<AdminBookingVO> page(Long userId, Long roomId, String bookingDate,
                                           Integer status, long pageNum, long pageSize) {
        LocalDate date = parseDate(bookingDate);
        Page<AdminBookingVO> page = (Page<AdminBookingVO>) bookingMapper.selectAdminBookingPage(
                new Page<>(pageNum, pageSize), userId, roomId, date, status);
        return PageResult.of(page);
    }

    @Override
    public void forceCancel(Long orderId, String reason, Long adminId) {
        BookingOrder order = requireOrder(orderId);
        if (order.getStatus() != OrderStatus.WAIT_CHECKIN && order.getStatus() != OrderStatus.IN_USE) {
            throw new BusinessException(ResultCode.BOOKING_STATE_ERROR, "仅待签到/进行中的订单可强制取消");
        }
        order.setStatus(OrderStatus.CANCELED);
        order.setRemark("管理员强制取消：" + reason);
        bookingMapper.updateById(order);

        String roomSeat = roomSeatText(order);
        sendMessage(order.getUserId(), "预约取消通知",
                "您的预约（" + order.getBookingDate() + " " + order.getStartTime().format(TIME_FMT)
                        + "-" + order.getEndTime().format(TIME_FMT) + " " + roomSeat
                        + "）已被管理员取消，原因：" + reason + "。如有疑问请联系图书馆管理员。");
    }

    @Override
    public void updateOrderTime(Long orderId, BookingTimeDTO dto, Long adminId) {
        BookingOrder order = requireOrder(orderId);
        if (order.getStatus() != OrderStatus.WAIT_CHECKIN && order.getStatus() != OrderStatus.IN_USE) {
            throw new BusinessException(ResultCode.BOOKING_STATE_ERROR, "仅待签到/进行中的订单可修改时间");
        }
        LocalDate newDate = parseDate(dto.getBookingDate());
        LocalTime newStart = parseTime(dto.getStartTime());
        LocalTime newEnd = parseTime(dto.getEndTime());
        if (!newStart.isBefore(newEnd)) {
            throw new BusinessException("开始时间必须早于结束时间");
        }
        // 开闭馆时间校验
        StudyRoom room = roomMapper.selectById(order.getRoomId());
        if (room == null) {
            throw new BusinessException("订单关联自习室不存在");
        }
        if (newStart.isBefore(room.getOpenTime()) || newEnd.isAfter(room.getCloseTime())) {
            throw new BusinessException("修改后的时段须在开放时间 " + room.getOpenTime().format(TIME_FMT)
                    + "-" + room.getCloseTime().format(TIME_FMT) + " 内");
        }
        // 该座位在目标时段的冲突校验（排除本订单）
        int conflict = bookingMapper.countSeatConflictExclude(order.getSeatId(), newDate,
                newStart, newEnd, orderId);
        if (conflict > 0) {
            throw new BusinessException(ResultCode.BOOKING_CONFLICT);
        }

        order.setBookingDate(newDate);
        order.setStartTime(newStart);
        order.setEndTime(newEnd);
        bookingMapper.updateById(order);

        String roomSeat = roomSeatText(order);
        sendMessage(order.getUserId(), "预约时间调整通知",
                "您的预约已由管理员调整为：" + newDate + " " + newStart.format(TIME_FMT)
                        + "-" + newEnd.format(TIME_FMT) + " " + roomSeat + "，请留意新的预约时间。");
    }

    @Override
    public void updateRemark(Long orderId, String remark) {
        BookingOrder order = requireOrder(orderId);
        order.setRemark(StringUtils.hasText(remark) ? remark : null);
        bookingMapper.updateById(order);
    }

    // ===== 私有方法 =====

    private BookingOrder requireOrder(Long orderId) {
        BookingOrder order = bookingMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return order;
    }

    private String roomSeatText(BookingOrder order) {
        StudyRoom room = roomMapper.selectById(order.getRoomId());
        StudySeat seat = seatMapper.selectById(order.getSeatId());
        return (room != null ? room.getRoomName() : "自习室")
                + (seat != null ? " " + seat.getSeatCode() + " 座位" : "");
    }

    private void sendMessage(Long userId, String title, String content) {
        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType("系统");
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }

    private LocalDate parseDate(String date) {
        if (!StringUtils.hasText(date)) {
            return null;
        }
        try {
            return LocalDate.parse(date, DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new BusinessException("日期格式错误，应为 yyyy-MM-dd");
        }
    }

    private LocalTime parseTime(String time) {
        try {
            return LocalTime.parse(time, TIME_FMT);
        } catch (DateTimeParseException e) {
            throw new BusinessException("时间格式错误，应为 HH:mm");
        }
    }
}
