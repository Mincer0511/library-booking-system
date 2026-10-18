package com.gxnu.library.controller;

import com.gxnu.library.common.result.Result;
import com.gxnu.library.entity.SysNotice;
import com.gxnu.library.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公告查看接口（用户端，白名单放行）
 */
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 已发布公告列表（置顶优先）
     */
    @GetMapping("/list")
    public Result<List<SysNotice>> list() {
        return Result.success(noticeService.listPublished());
    }

    /**
     * 公告详情
     */
    @GetMapping("/detail/{id}")
    public Result<SysNotice> detail(@PathVariable("id") Long id) {
        return Result.success(noticeService.detail(id));
    }
}
