package com.gxnu.library.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 座位表
 */
@Data
@TableName("study_seat")
public class StudySeat {

    @TableId(type = IdType.AUTO)
    private Long seatId;

    /** 所属自习室ID */
    private Long roomId;

    /** 座位编号 */
    private String seatCode;

    /** 座位类型：靠窗/带插座/普通 */
    private String seatType;

    /** 平面图X坐标 */
    private Integer positionX;

    /** 平面图Y坐标 */
    private Integer positionY;

    /** 是否有插座 */
    private Integer hasSocket;

    /** 状态：0不可用 1可用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
