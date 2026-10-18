package com.gxnu.library.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端禁用记录 VO（联查用户账号姓名）
 */
@Data
public class DisableRecordVO {

    private Long disableId;
    private Long userId;
    private String username;
    private String realName;
    private String disableReason;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer isLifted;
    private Long liftBy;
    private LocalDateTime liftTime;
    private LocalDateTime createTime;
}
