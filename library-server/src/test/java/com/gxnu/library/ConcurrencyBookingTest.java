package com.gxnu.library;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.dto.BookingCreateDTO;
import com.gxnu.library.entity.BookingOrder;
import com.gxnu.library.entity.SysUser;
import com.gxnu.library.mapper.BookingOrderMapper;
import com.gxnu.library.mapper.SysUserMapper;
import com.gxnu.library.service.BookingService;
import com.gxnu.library.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * B3-11 并发预约压测：
 * 50 个用户同一时刻并发预约同一座位同一时段，利用数据库行锁
 * （countSeatConflictForUpdate 串行化校验）断言仅 1 单成功。
 * 测试账号与订单在用例结束后全部清理。
 */
@SpringBootTest
class ConcurrencyBookingTest {

    private static final String USERNAME_PREFIX = "TEST_CC_";
    private static final int THREADS = 50;
    private static final Long SEAT_ID = 1L;

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingOrderMapper bookingMapper;
    @Autowired
    private SysUserMapper userMapper;

    private final List<Long> testUserIds = new ArrayList<>();
    private Long testUserId;

    @BeforeEach
    void setUp() {
        // 复用或创建 0 号主测试用户（发起并发中第一个请求的候选）
        testUserId = prepareUser(0);
        testUserIds.add(testUserId);
        // 创建 1~49 号并发用户
        for (int i = 1; i < THREADS; i++) {
            testUserIds.add(prepareUser(i));
        }
    }

    @AfterEach
    void tearDown() {
        // 清理全部测试订单与测试用户，避免污染演示数据
        bookingMapper.delete(new LambdaQueryWrapper<BookingOrder>()
                .in(BookingOrder::getUserId, testUserIds));
        userMapper.deleteByIds(testUserIds);
    }

    /** 创建（或重置）一个可预约状态的测试用户 */
    private Long prepareUser(int index) {
        String username = USERNAME_PREFIX + index;
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            user = new SysUser();
            user.setUsername(username);
            user.setPassword("test");
            user.setName("并发测试" + index);
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
        return user.getUserId();
    }

    @Test
    void concurrentBooking_sameSeatSameSlot_onlyOneSuccess() throws Exception {
        // 目标时段：明天 10:00-11:00（在可预约日期范围内、自习室开放时间内）
        BookingCreateDTO dto = new BookingCreateDTO();
        dto.setSeatId(SEAT_ID);
        dto.setBookingDate(LocalDate.now().plusDays(1));
        dto.setStartTime("10:00");
        dto.setEndTime("11:00");

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < THREADS; i++) {
            final Long userId = testUserIds.get(i);
            futures.add(pool.submit(() -> {
                // 每个并发线程绑定独立用户身份（service 层通过 UserContext 取当前用户）
                UserContext.set(new UserContext.LoginUser(userId, USERNAME_PREFIX + userId, RoleKey.STUDENT));
                ready.countDown();
                try {
                    start.await();
                    bookingService.create(dto);
                    success.incrementAndGet();
                } catch (Exception ignored) {
                    // 并发失败方预期抛出座位冲突 / 其他业务异常
                } finally {
                    UserContext.clear();
                }
            }));
        }

        // 全部线程就绪后同时放行，制造真实并发竞争
        ready.await();
        start.countDown();
        for (Future<?> future : futures) {
            future.get();
        }
        pool.shutdown();

        Assertions.assertEquals(1, success.get(),
                "同一座位同一时段并发预约50次，应仅有1单成功（行锁防超卖）");
    }
}
