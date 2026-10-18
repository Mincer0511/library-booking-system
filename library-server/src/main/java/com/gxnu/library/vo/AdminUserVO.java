package com.gxnu.library.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端用户列表 VO（不含密码，联查角色名）
 */
@Data
public class AdminUserVO {

    private Long userId;
    private String username;
    private String name;
    private String nickname;
    private String phone;
    private String avatar;
    private Long roleId;
    private String roleName;
    private Integer creditScore;
    private Integer isDisabled;
    private LocalDateTime disableEndTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
