package com.gxnu.library.service;

import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.dto.BookingTimeDTO;
import com.gxnu.library.vo.AdminBookingVO;

/**
 * 管理端订单运营 Service
 */
public interface AdminBookingService {

    /** 订单分页：用户/自习室/日期/状态组合筛选 */
    PageResult<AdminBookingVO> page(Long userId, Long roomId, String bookingDate,
                                    Integer status, long pageNum, long pageSize);

    /** 强制取消（备注留痕 + 站内通知用户） */
    void forceCancel(Long orderId, String reason, Long adminId);

    /** 修改订单时间（开闭馆与冲突校验 + 站内通知用户） */
    void updateOrderTime(Long orderId, BookingTimeDTO dto, Long adminId);

    /** 修改备注 */
    void updateRemark(Long orderId, String remark);
}
