package com.gxnu.library.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约订单视图（联查座位/自习室/楼层）
 */
@Data
public class BookingVO {

    private Long orderId;
    private Long userId;
    private Long seatId;
    private String seatCode;
    private Long roomId;
    private String roomName;
    private Integer floorNum;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer status;
    private String statusText;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private String remark;
    private LocalDateTime createTime;
    /** 是否可取消（待签到且未开始），服务端计算 */
    private Boolean canCancel;
    /** 是否可签到（待签到且在签到窗口内），服务端计算 */
    private Boolean canCheckin;
    /** 是否可签退（进行中），服务端计算 */
    private Boolean canCheckout;
    /** 是否可续时（进行中且未过结束时间），服务端计算 */
    private Boolean canExtend;
}
