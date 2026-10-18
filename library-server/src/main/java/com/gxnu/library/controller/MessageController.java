package com.gxnu.library.controller;

import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.Result;
import com.gxnu.library.entity.SysMessage;
import com.gxnu.library.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内消息接口
 */
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 我的消息分页（isRead 可选：0未读 1已读）
     */
    @GetMapping("/page")
    public Result<PageResult<SysMessage>> page(@RequestParam(required = false) Integer isRead,
                                               @RequestParam(defaultValue = "1") long pageNum,
                                               @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(messageService.myPage(isRead, pageNum, pageSize));
    }

    /**
     * 未读消息数（顶部导航角标）
     */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(messageService.unreadCount());
    }

    /**
     * 标记单条已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable("id") Long id) {
        messageService.markRead(id);
        return Result.success();
    }

    /**
     * 全部标记已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        messageService.markAllRead();
        return Result.success();
    }
}
