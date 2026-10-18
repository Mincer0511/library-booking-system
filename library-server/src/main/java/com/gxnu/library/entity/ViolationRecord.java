package com.gxnu.library.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 违规记录表
 */
@Data
@TableName("violation_record")
public class ViolationRecord {

    @TableId(type = IdType.AUTO)
    private Long violationId;

    /** 用户ID */
    private Long userId;

    /** 关联订单ID */
    private Long orderId;

    /** 违规类型：爽约/占座/其他 */
    private String violationType;

    /** 违规描述 */
    private String violationDesc;

    /** 扣减信用分 */
    private Integer deductScore;

    /** 操作人（NULL为系统） */
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
