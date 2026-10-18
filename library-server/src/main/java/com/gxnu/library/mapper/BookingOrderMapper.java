package com.gxnu.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnu.library.entity.BookingOrder;
import com.gxnu.library.vo.BookingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 预约订单 Mapper
 */
@Mapper
public interface BookingOrderMapper extends BaseMapper<BookingOrder> {

    /**
     * 座位时段冲突校验（行锁，防并发重复下单）
     * 区间相交：已有.start < 新.end AND 已有.end > 新.start
     */
    int countSeatConflictForUpdate(@Param("seatId") Long seatId,
                                   @Param("bookingDate") LocalDate bookingDate,
                                   @Param("startTime") LocalTime startTime,
                                   @Param("endTime") LocalTime endTime);

    /**
     * 同一用户同时段冲突校验
     */
    int countUserTimeConflict(@Param("userId") Long userId,
                              @Param("bookingDate") LocalDate bookingDate,
                              @Param("startTime") LocalTime startTime,
                              @Param("endTime") LocalTime endTime);

    /**
     * 续时冲突校验：座位时段冲突（排除指定订单本身）
     */
    int countSeatConflictExclude(@Param("seatId") Long seatId,
                                 @Param("bookingDate") LocalDate bookingDate,
                                 @Param("startTime") LocalTime startTime,
                                 @Param("endTime") LocalTime endTime,
                                 @Param("excludeOrderId") Long excludeOrderId);

    /**
     * 我的预约分页（联查座位/自习室/楼层）
     */
    IPage<BookingVO> selectMyBookingPage(Page<BookingVO> page,
                                         @Param("userId") Long userId,
                                         @Param("status") Integer status);

    /**
     * 我的预约详情
     */
    BookingVO selectMyBookingDetail(@Param("orderId") Long orderId,
                                    @Param("userId") Long userId);

    /**
     * 管理端订单分页（联查用户/座位/自习室/楼层，多条件筛选）
     */
    IPage<com.gxnu.library.vo.AdminBookingVO> selectAdminBookingPage(
            Page<com.gxnu.library.vo.AdminBookingVO> page,
            @Param("userId") Long userId,
            @Param("roomId") Long roomId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("status") Integer status);
}
