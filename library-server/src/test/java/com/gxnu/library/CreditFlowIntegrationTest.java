package com.gxnu.library;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxnu.library.common.constant.OrderStatus;
import com.gxnu.library.entity.BookingOrder;
import com.gxnu.library.entity.DisableRecord;
import com.gxnu.library.entity.SysMessage;
import com.gxnu.library.entity.SysUser;
import com.gxnu.library.entity.ViolationRecord;
import com.gxnu.library.mapper.BookingOrderMapper;
import com.gxnu.library.mapper.DisableRecordMapper;
import com.gxnu.library.mapper.SysMessageMapper;
import com.gxnu.library.mapper.SysUserMapper;
import com.gxnu.library.mapper.ViolationRecordMapper;
import com.gxnu.library.schedule.BookingSchedule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * B2-10 信用分全链路集成测试：
 * 超时未签到 → 爽约扫描 → 扣分+违规记录+站内信 → 低于阈值自动禁用 → 到期自动解禁+恢复60分
 *
 * 使用独立测试账号（TEST_FLOW），测试数据在用例结束后清理。
 */
@SpringBootTest
class CreditFlowIntegrationTest {

    private static final String TEST_USERNAME = "TEST_FLOW";

    @Autowired
    private BookingSchedule bookingSchedule;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private BookingOrderMapper bookingMapper;
    @Autowired
    private ViolationRecordMapper violationMapper;
    @Autowired
    private DisableRecordMapper disableMapper;
    @Autowired
    private SysMessageMapper messageMapper;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        // 复用或创建测试用户（信用分 100、学生角色）
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, TEST_USERNAME));
        if (user == null) {
            user = new SysUser();
            user.setUsername(TEST_USERNAME);
            user.setPassword("test");
            user.setName("测试链路");
            user.setRoleId(1L);
            user.setCreditScore(100);
            user.setIsDisabled(0);
            user.setStatus(1);
            userMapper.insert(user);
        } else {
            user.setCreditScore(100);
            user.setIsDisabled(0);
            user.setDisableEndTime(null);
            userMapper.updateById(user);
        }
        testUserId = user.getUserId();
    }

    @AfterEach
    void tearDown() {
        // 清理测试产生的数据，避免污染演示数据
        List<BookingOrder> orders = bookingMapper.selectList(
                new LambdaQueryWrapper<BookingOrder>().eq(BookingOrder::getUserId, testUserId));
        bookingMapper.delete(new LambdaQueryWrapper<BookingOrder>().eq(BookingOrder::getUserId, testUserId));
        violationMapper.delete(new LambdaQueryWrapper<ViolationRecord>().eq(ViolationRecord::getUserId, testUserId));
        disableMapper.delete(new LambdaQueryWrapper<DisableRecord>().eq(DisableRecord::getUserId, testUserId));
        messageMapper.delete(new LambdaQueryWrapper<SysMessage>().eq(SysMessage::getUserId, testUserId));
    }

    /** 插入一个"已开始且超过签到窗口"的待签到订单 */
    private Long insertOverdueOrder() {
        BookingOrder order = new BookingOrder();
        order.setUserId(testUserId);
        order.setSeatId(1L);
        order.setRoomId(1L);
        order.setBookingDate(LocalDate.now());
        order.setStartTime(LocalTime.of(0, 0));
        order.setEndTime(LocalTime.of(2, 0));
        order.setStatus(OrderStatus.WAIT_CHECKIN);
        bookingMapper.insert(order);
        return order.getOrderId();
    }

    @Test
    void noShow_shouldDeductCreditAndNotify() {
        Long orderId = insertOverdueOrder();

        bookingSchedule.scanNoShow();

        // 订单置为已爽约
        BookingOrder order = bookingMapper.selectById(orderId);
        Assertions.assertEquals(OrderStatus.NO_SHOW, order.getStatus(), "订单应置为已爽约");
        // 信用分 100 -> 90
        SysUser user = userMapper.selectById(testUserId);
        Assertions.assertEquals(90, user.getCreditScore(), "爽约应扣10分");
        Assertions.assertEquals(0, user.getIsDisabled(), "90分不应触发禁用");
        // 违规记录
        Long vioCount = violationMapper.selectCount(
                new LambdaQueryWrapper<ViolationRecord>().eq(ViolationRecord::getUserId, testUserId));
        Assertions.assertEquals(1, vioCount, "应生成1条违规记录");
        // 站内通知
        Long msgCount = messageMapper.selectCount(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getUserId, testUserId).eq(SysMessage::getTitle, "违规通知"));
        Assertions.assertEquals(1, msgCount, "应收到违规通知");
    }

    @Test
    void lowCredit_shouldAutoDisable() {
        // 65 分爽约一次 → 55 < 60 触发禁用
        SysUser user = userMapper.selectById(testUserId);
        user.setCreditScore(65);
        userMapper.updateById(user);
        insertOverdueOrder();

        bookingSchedule.scanNoShow();

        SysUser after = userMapper.selectById(testUserId);
        Assertions.assertEquals(55, after.getCreditScore());
        Assertions.assertEquals(1, after.getIsDisabled(), "低于60分应自动禁用");
        Assertions.assertNotNull(after.getDisableEndTime(), "应记录解禁时间");
        Long disableCount = disableMapper.selectCount(
                new LambdaQueryWrapper<DisableRecord>().eq(DisableRecord::getUserId, testUserId));
        Assertions.assertEquals(1, disableCount, "应生成禁用记录");
    }

    @Test
    void expiredDisable_shouldAutoLiftAndRestore() {
        // 构造：已禁用且禁用记录已到期
        SysUser user = userMapper.selectById(testUserId);
        user.setCreditScore(55);
        user.setIsDisabled(1);
        user.setDisableEndTime(LocalDateTime.now().minusMinutes(1));
        userMapper.updateById(user);

        DisableRecord record = new DisableRecord();
        record.setUserId(testUserId);
        record.setDisableReason("测试禁用");
        record.setStartTime(LocalDateTime.now().minusDays(7));
        record.setEndTime(LocalDateTime.now().minusMinutes(1));
        record.setIsLifted(0);
        disableMapper.insert(record);

        bookingSchedule.scanLiftDisable();

        SysUser after = userMapper.selectById(testUserId);
        Assertions.assertEquals(0, after.getIsDisabled(), "到期应自动解禁");
        Assertions.assertEquals(60, after.getCreditScore(), "解禁后信用分应恢复至60");
        Assertions.assertNull(after.getDisableEndTime(), "解禁后禁用到期时间应清空");
        DisableRecord lifted = disableMapper.selectById(record.getDisableId());
        Assertions.assertEquals(1, lifted.getIsLifted(), "禁用记录应标记已解除");
        Assertions.assertNotNull(lifted.getLiftTime(), "应记录解除时间");
    }

    @Test
    void finishedOrder_shouldAutoComplete() {
        // 进行中订单结束时间已过 → 自动完成
        BookingOrder order = new BookingOrder();
        order.setUserId(testUserId);
        order.setSeatId(1L);
        order.setRoomId(1L);
        order.setBookingDate(LocalDate.now());
        order.setStartTime(LocalTime.of(0, 0));
        order.setEndTime(LocalTime.of(0, 30));
        order.setStatus(OrderStatus.IN_USE);
        order.setCheckinTime(LocalDateTime.now().minusHours(1));
        bookingMapper.insert(order);

        bookingSchedule.scanAutoFinish();

        BookingOrder after = bookingMapper.selectById(order.getOrderId());
        Assertions.assertEquals(OrderStatus.FINISHED, after.getStatus(), "超时进行中订单应自动完成");
        Assertions.assertNotNull(after.getCheckoutTime(), "应记录自动签退时间");
    }
}
