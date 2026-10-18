package com.gxnu.library.vo;

import lombok.Data;

import java.time.LocalTime;

/**
 * 自习室卡片（含实时空余座位数）
 */
@Data
public class RoomListVO {

    private Long roomId;
    private Long floorId;
    private Integer floorNum;
    private String roomName;
    private Integer seatCount;
    /** 可用座位数 */
    private Integer usableSeats;
    /** 当前时刻空余座位数 */
    private Integer freeNow;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String roomType;
    private String description;
    private Integer status;
}
