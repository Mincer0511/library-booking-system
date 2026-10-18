package com.gxnu.library.service;

import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.dto.BookingCreateDTO;
import com.gxnu.library.dto.BookingExtendDTO;
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
     * 签到（仅本人，签到窗口 [开始时间, 开始时间+迟到分钟数] 内）
     */
    void checkin(Long orderId);

    /**
     * 签退（仅本人，进行中可签退，即时释放座位）
     */
    void checkout(Long orderId);

    /**
     * 续时（仅本人，进行中可延长结束时间，需校验延长时段座位空闲与总时长上限）
     */
    void extend(Long orderId, BookingExtendDTO dto);

    /**
     * 将超时未签到订单判定为爽约（定时任务调用：状态置4 + 信用扣分 + 禁用判定 + 站内通知）
     */
    void markNoShow(Long orderId);

    /**
     * 我的预约分页
     */
    PageResult<BookingVO> myPage(Integer status, long pageNum, long pageSize);

    /**
     * 我的预约详情
     */
    BookingVO myDetail(Long orderId);
}
