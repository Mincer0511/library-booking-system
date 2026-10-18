package com.gxnu.library.service;

import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.entity.SysMessage;

/**
 * 站内消息 Service
 */
public interface MessageService {

    /**
     * 我的消息分页（isRead 为 null 查全部）
     */
    PageResult<SysMessage> myPage(Integer isRead, long pageNum, long pageSize);

    /**
     * 我的未读消息数
     */
    long unreadCount();

    /**
     * 标记单条已读（仅本人）
     */
    void markRead(Long msgId);

    /**
     * 全部标记已读
     */
    void markAllRead();
}
