package com.gxnu.library.vo;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

/**
 * 座位平面图（含当日各座位预约状态）
 */
@Data
public class SeatMapVO {

    private Long roomId;
    private String roomName;
    private Integer floorNum;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String roomType;
    private List<SeatItem> seats;

    /**
     * 单个座位
     */
    @Data
    public static class SeatItem {
        private Long seatId;
        private String seatCode;
        private String seatType;
        private Integer hasSocket;
        private Integer positionX;
        private Integer positionY;
        /** 座位本身是否可用 */
        private Boolean usable;
        /** 当日有效预约（待签到/进行中） */
        private List<SeatBooking> bookings;
    }

    /**
     * 座位当日预约片段
     */
    @Data
    public static class SeatBooking {
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer status;
        /** 是否当前登录用户本人的预约 */
        private Boolean mine;
    }
}
