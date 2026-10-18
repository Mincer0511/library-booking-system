package com.gxnu.library.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long userId;

    /** 学号/登录账号 */
    private String username;

    /** 加密密码（不返回给前端） */
    @JsonIgnore
    private String password;

    /** 真实姓名 */
    private String name;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 头像地址 */
    private String avatar;

    /** 角色ID */
    private Long roleId;

    /** 信用分 */
    private Integer creditScore;

    /** 是否禁用预约：0否 1是 */
    private Integer isDisabled;

    /** 禁用到期时间 */
    private LocalDateTime disableEndTime;

    /** 账号状态：0禁用 1正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
