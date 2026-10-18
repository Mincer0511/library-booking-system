package com.gxnu.library.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 楼层表
 */
@Data
@TableName("library_floor")
public class LibraryFloor {

    @TableId(type = IdType.AUTO)
    private Long floorId;

    /** 楼层号（1-5） */
    private Integer floorNum;

    /** 楼层名称 */
    private String floorName;

    /** 楼层说明 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
