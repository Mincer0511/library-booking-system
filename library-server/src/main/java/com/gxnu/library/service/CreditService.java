package com.gxnu.library.service;

import com.gxnu.library.entity.ViolationRecord;
import com.gxnu.library.vo.CreditVO;

import java.util.List;

/**
 * 信用分 Service：扣分 + 违规记录 + 禁用判定 + 解禁（事务）
 */
public interface CreditService {

    /**
     * 扣分：扣减信用分 + 生成违规记录 + 站内通知 + 低于阈值触发禁用（同一事务）
     *
     * @param userId       用户ID
     * @param orderId      关联订单ID（无关联传 null）
     * @param violationType 违规类型：爽约/占座/其他
     * @param desc         违规描述
     * @param deductScore  扣减分数
     * @param createBy     操作人ID（系统操作传 null）
     */
    void deduct(Long userId, Long orderId, String violationType, String desc, int deductScore, Long createBy);

    /**
     * 解除禁用：恢复信用分至配置值，更新禁用记录（定时任务自动解禁 / 超管手动解禁共用）
     *
     * @param userId 用户ID
     * @param liftBy 解除操作人ID（系统自动解禁传 null）
     */
    void liftDisable(Long userId, Long liftBy);

    /**
     * 当前登录用户的信用面板（分数、禁用状态、解禁时间、近期扣分记录）
     */
    CreditVO myPanel();

    /**
     * 当前登录用户的全部违规记录（时间倒序）
     */
    List<ViolationRecord> myViolations();
}
