package com.gxnu.library.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 自习室利用率统计 VO（日 × 自习室维度）
 */
@Data
public class RoomUtilizationVO {

    /** 预约日期 */
    private LocalDate date;

    private Long roomId;

    private String roomName;

    /** 楼层号 */
    private Integer floorNum;

    /** 座位总数 */
    private Integer totalSeats;

    /** 已预约总时长（分钟） */
    private Long bookedMinutes;

    /** 利用率（%） */
    private Double utilization;
}
