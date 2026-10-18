package com.gxnu.library.vo;

import com.gxnu.library.entity.ViolationRecord;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 我的信用面板
 */
@Data
public class CreditVO {

    /** 当前信用分 */
    private Integer creditScore;

    /** 是否禁用预约：0否 1是 */
    private Integer isDisabled;

    /** 禁用到期时间（未禁用为 null） */
    private LocalDateTime disableEndTime;

    /** 禁用阈值（低于该分数禁用，供前端展示） */
    private Integer disableThreshold;

    /** 近期扣分/违规记录（时间倒序，最多10条） */
    private List<ViolationRecord> recentViolations;
}
