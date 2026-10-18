package com.gxnu.library.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gxnu.library.common.constant.ConfigKey;
import com.gxnu.library.common.constant.OrderStatus;
import com.gxnu.library.entity.BookingOrder;
import com.gxnu.library.entity.DisableRecord;
import com.gxnu.library.entity.StudyRoom;
import com.gxnu.library.entity.StudySeat;
import com.gxnu.library.entity.SysMessage;
import com.gxnu.library.mapper.BookingOrderMapper;
import com.gxnu.library.mapper.DisableRecordMapper;
import com.gxnu.library.mapper.StudyRoomMapper;
import com.gxnu.library.mapper.StudySeatMapper;
import com.gxnu.library.mapper.SysMessageMapper;
import com.gxnu.library.service.BookingService;
import com.gxnu.library.service.ConfigService;
import com.gxnu.library.service.CreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 预约相关定时任务：爽约扫描 / 自动解禁 / 自动完成 / 预约提醒 / 临期提醒（CR-001）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingSchedule {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final BookingOrderMapper bookingMapper;
    private final DisableRecordMapper disableMapper;
    private final StudyRoomMapper roomMapper;
    private final StudySeatMapper seatMapper;
    private final SysMessageMapper messageMapper;
    private final BookingService bookingService;
    private final CreditService creditService;
    private final ConfigService configService;

    /**
     * 爽约扫描（每 1 分钟）：超过签到时限仍未签到的待签到订单 → 爽约 + 扣分 + 禁用判定
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void scanNoShow() {
        int lateMinutes = configService.getInt(ConfigKey.CHECKIN_LATE_MINUTES, 30);
        LocalDateTime now = LocalDateTime.now();
        // 待签到且预约日期不晚于今天，逐个判断签到窗口是否已过
        List<BookingOrder> waiting = bookingMapper.selectList(new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getStatus, OrderStatus.WAIT_CHECKIN)
                .le(BookingOrder::getBookingDate, LocalDate.now()));
        for (BookingOrder order : waiting) {
            LocalDateTime deadline = LocalDateTime.of(order.getBookingDate(), order.getStartTime())
                    .plusMinutes(lateMinutes);
            if (now.isAfter(deadline)) {
                try {
                    bookingService.markNoShow(order.getOrderId());
                    log.info("[定时任务] 订单 {} 超时未签到，判定爽约", order.getOrderId());
                } catch (Exception e) {
                    log.error("[定时任务] 订单 {} 爽约处理失败: {}", order.getOrderId(), e.getMessage());
                }
            }
        }
    }

    /**
     * 自动解禁（每 1 分钟）：禁用到期记录 → 解除禁用 + 恢复信用分
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 40_000)
    public void scanLiftDisable() {
        List<DisableRecord> expired = disableMapper.selectList(new LambdaQueryWrapper<DisableRecord>()
                .eq(DisableRecord::getIsLifted, 0)
                .le(DisableRecord::getEndTime, LocalDateTime.now()));
        for (DisableRecord record : expired) {
            try {
                creditService.liftDisable(record.getUserId(), null);
                log.info("[定时任务] 用户 {} 禁用到期，自动解禁", record.getUserId());
            } catch (Exception e) {
                log.error("[定时任务] 用户 {} 自动解禁失败: {}", record.getUserId(), e.getMessage());
            }
        }
    }

    /**
     * 自动完成（每 5 分钟）：进行中订单结束时间已过 → 自动签退置为已完成
     */
    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    public void scanAutoFinish() {
        LocalDateTime now = LocalDateTime.now();
        List<BookingOrder> inUse = bookingMapper.selectList(new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getStatus, OrderStatus.IN_USE)
                .le(BookingOrder::getBookingDate, LocalDate.now()));
        for (BookingOrder order : inUse) {
            LocalDateTime endDateTime = LocalDateTime.of(order.getBookingDate(), order.getEndTime());
            if (!now.isBefore(endDateTime)) {
                order.setStatus(OrderStatus.FINISHED);
                order.setCheckoutTime(endDateTime);
                bookingMapper.updateById(order);
                log.info("[定时任务] 订单 {} 结束时间已过，自动完成", order.getOrderId());
            }
        }
    }

    /**
     * 预约提醒（每 5 分钟）：开始前 10~15 分钟的待签到订单推送站内提醒。
     * 扫描窗口宽度与任务周期一致（5 分钟），每个订单恰好命中一次，无需额外标记位。
     */
    @Scheduled(fixedDelay = 300_000, initialDelay = 50_000)
    public void scanBookingRemind() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalTime windowStart = now.plusMinutes(10).toLocalTime();
        LocalTime windowEnd = now.plusMinutes(15).toLocalTime();

        List<BookingOrder> upcoming = bookingMapper.selectList(new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getStatus, OrderStatus.WAIT_CHECKIN)
                .eq(BookingOrder::getBookingDate, today)
                .ge(BookingOrder::getStartTime, windowStart)
                .lt(BookingOrder::getStartTime, windowEnd));
        for (BookingOrder order : upcoming) {
            StudyRoom room = roomMapper.selectById(order.getRoomId());
            StudySeat seat = seatMapper.selectById(order.getSeatId());
            String roomName = room != null ? room.getRoomName() : "自习室";
            String seatCode = seat != null ? seat.getSeatCode() : "";

            SysMessage message = new SysMessage();
            message.setUserId(order.getUserId());
            message.setTitle("预约提醒");
            message.setContent("您预约的 " + roomName + " " + seatCode + " 座位将于今天 "
                    + order.getStartTime().format(TIME_FMT) + " 开始，请按时到场签到，"
                    + "开始起 " + configService.getInt(ConfigKey.CHECKIN_LATE_MINUTES, 30)
                    + " 分钟内未签到将判定爽约并扣除信用分。");
            message.setType("预约");
            message.setIsRead(0);
            messageMapper.insert(message);
            log.info("[定时任务] 订单 {} 预约开始前提醒已发送", order.getOrderId());
        }
    }

    /**
     * 临期提醒（每 1 分钟，CR-001）：进行中订单在结束前约 10 分钟推送站内信，
     * 提醒及时签退或续时。扫描窗口 now ∈ [end-12min, end-8min]（宽度 4 分钟 > 周期 1 分钟），
     * 配合 expire_notified 标记位保证每个订单只提醒一次。
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 20_000)
    public void scanExpiringSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime windowStart = now.plusMinutes(8).toLocalTime();
        LocalTime windowEnd = now.plusMinutes(12).toLocalTime();

        List<BookingOrder> expiring = bookingMapper.selectList(new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getStatus, OrderStatus.IN_USE)
                .eq(BookingOrder::getExpireNotified, 0)
                .eq(BookingOrder::getBookingDate, LocalDate.now())
                .ge(BookingOrder::getEndTime, windowStart)
                .le(BookingOrder::getEndTime, windowEnd));
        for (BookingOrder order : expiring) {
            try {
                StudyRoom room = roomMapper.selectById(order.getRoomId());
                StudySeat seat = seatMapper.selectById(order.getSeatId());
                String roomName = room != null ? room.getRoomName() : "自习室";
                String seatCode = seat != null ? seat.getSeatCode() : "";

                SysMessage message = new SysMessage();
                message.setUserId(order.getUserId());
                message.setTitle("预约即将到期");
                message.setContent("您在 " + roomName + " " + seatCode + " 座位的预约将于 "
                        + order.getEndTime().format(TIME_FMT) + " 结束，剩余约 10 分钟，"
                        + "请合理安排时间，如需继续使用请及时续时或重新预约。");
                message.setType("提醒");
                message.setIsRead(0);
                messageMapper.insert(message);
                // 标记已提醒，避免重复推送
                bookingMapper.update(null, new LambdaUpdateWrapper<BookingOrder>()
                        .eq(BookingOrder::getOrderId, order.getOrderId())
                        .set(BookingOrder::getExpireNotified, 1));
                log.info("[定时任务] 订单 {} 临期提醒已发送（CR-001）", order.getOrderId());
            } catch (Exception e) {
                log.error("[定时任务] 订单 {} 临期提醒发送失败: {}", order.getOrderId(), e.getMessage());
            }
        }
    }
}
