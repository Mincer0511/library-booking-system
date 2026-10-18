package com.gxnu.library.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约订单表
 */
@Data
@TableName("booking_order")
public class BookingOrder {

    @TableId(type = IdType.AUTO)
    private Long orderId;

    /** 用户ID */
    private Long userId;

    /** 座位ID */
    private Long seatId;

    /** 自习室ID */
    private Long roomId;

    /** 预约日期 */
    private LocalDate bookingDate;

    /** 开始时间 */
    private LocalTime startTime;

    /** 结束时间 */
    private LocalTime endTime;

    /** 状态：0待签到 1进行中 2已完成 3已取消 4已爽约 */
    private Integer status;

    /** 签到时间 */
    private LocalDateTime checkinTime;

    /** 签退时间 */
    private LocalDateTime checkoutTime;

    /** 是否已发送临期提醒：0否 1是（CR-001） */
    private Integer expireNotified;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
