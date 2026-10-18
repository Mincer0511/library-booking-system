package com.gxnu.library.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 管理端订单列表 VO（联查用户/座位/自习室/楼层）
 */
@Data
public class AdminBookingVO {

    private Long orderId;
    private Long userId;
    private String username;
    private String realName;
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
}
