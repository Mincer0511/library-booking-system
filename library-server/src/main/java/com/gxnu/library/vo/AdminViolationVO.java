package com.gxnu.library.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端违规记录 VO（联查用户账号姓名）
 */
@Data
public class AdminViolationVO {

    private Long violationId;
    private Long userId;
    private String username;
    private String realName;
    private Long orderId;
    private String violationType;
    private String violationDesc;
    private Integer deductScore;
    private Long createBy;
    private LocalDateTime createTime;
}
