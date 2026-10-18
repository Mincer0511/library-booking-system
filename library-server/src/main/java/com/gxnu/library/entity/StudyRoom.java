package com.gxnu.library.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 自习室表
 */
@Data
@TableName("study_room")
public class StudyRoom {

    @TableId(type = IdType.AUTO)
    private Long roomId;

    /** 所属楼层ID */
    private Long floorId;

    /** 自习室名称 */
    private String roomName;

    /** 座位总数 */
    private Integer seatCount;

    /** 开放时间 */
    private LocalTime openTime;

    /** 关闭时间 */
    private LocalTime closeTime;

    /** 类型：普通/静音/研讨 */
    private String roomType;

    /** 简介说明 */
    private String description;

    /** 状态：0关闭 1开放 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
