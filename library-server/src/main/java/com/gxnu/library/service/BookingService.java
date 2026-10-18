package com.gxnu.library.service;

import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.dto.BookingCreateDTO;
import com.gxnu.library.vo.BookingVO;

/**
 * 预约 Service
 */
public interface BookingService {

    /**
     * 提交预约（含全部规则校验与冲突校验），返回订单ID
     */
    Long create(BookingCreateDTO dto);

    /**
     * 取消预约（仅本人，开始前可取消）
     */
    void cancel(Long orderId);

    /**
     * 我的预约分页
     */
    PageResult<BookingVO> myPage(Integer status, long pageNum, long pageSize);

    /**
     * 我的预约详情
     */
    BookingVO myDetail(Long orderId);
}
