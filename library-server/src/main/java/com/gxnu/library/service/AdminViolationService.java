package com.gxnu.library.service;

import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.dto.ViolationAddDTO;
import com.gxnu.library.vo.AdminViolationVO;

/**
 * 管理端违规管理 Service
 */
public interface AdminViolationService {

    /** 违规记录分页（联查用户） */
    PageResult<AdminViolationVO> page(Long userId, String violationType,
                                      long pageNum, long pageSize);

    /** 手动添加违规：联动扣分 + 禁用判定 + 站内通知 */
    void add(ViolationAddDTO dto, Long adminId);

    /** 撤销违规：恢复分数 + 删除记录 + 必要时联动解禁 + 站内通知 */
    void revoke(Long violationId, Long adminId);
}
