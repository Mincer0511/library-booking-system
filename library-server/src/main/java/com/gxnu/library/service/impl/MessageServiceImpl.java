package com.gxnu.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.ResultCode;
import com.gxnu.library.entity.SysMessage;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.SysMessageMapper;
import com.gxnu.library.service.MessageService;
import com.gxnu.library.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 站内消息 Service 实现
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final SysMessageMapper messageMapper;

    @Override
    public PageResult<SysMessage> myPage(Integer isRead, long pageNum, long pageSize) {
        Long userId = UserContext.currentUserId();
        Page<SysMessage> page = messageMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysMessage>()
                        .eq(SysMessage::getUserId, userId)
                        .eq(isRead != null, SysMessage::getIsRead, isRead)
                        .orderByDesc(SysMessage::getCreateTime));
        return PageResult.of(page);
    }

    @Override
    public long unreadCount() {
        Long userId = UserContext.currentUserId();
        return messageMapper.selectCount(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getUserId, userId)
                .eq(SysMessage::getIsRead, 0));
    }

    @Override
    public void markRead(Long msgId) {
        Long userId = UserContext.currentUserId();
        SysMessage message = messageMapper.selectById(msgId);
        if (message == null || !message.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "消息不存在");
        }
        if (message.getIsRead() == 0) {
            message.setIsRead(1);
            messageMapper.updateById(message);
        }
    }

    @Override
    public void markAllRead() {
        Long userId = UserContext.currentUserId();
        messageMapper.update(null, new LambdaUpdateWrapper<SysMessage>()
                .eq(SysMessage::getUserId, userId)
                .eq(SysMessage::getIsRead, 0)
                .set(SysMessage::getIsRead, 1));
    }
}
