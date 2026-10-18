package com.gxnu.library.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 禁用记录表
 */
@Data
@TableName("disable_record")
public class DisableRecord {

    @TableId(type = IdType.AUTO)
    private Long disableId;

    /** 用户ID */
    private Long userId;

    /** 禁用原因 */
    private String disableReason;

    /** 禁用开始时间 */
    private LocalDateTime startTime;

    /** 禁用结束时间 */
    private LocalDateTime endTime;

    /** 是否已解除：0否 1是 */
    private Integer isLifted;

    /** 解除操作人（NULL为系统） */
    private Long liftBy;

    /** 解除时间 */
    private LocalDateTime liftTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
