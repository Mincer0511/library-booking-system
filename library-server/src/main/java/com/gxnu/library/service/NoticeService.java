package com.gxnu.library.service;

import com.gxnu.library.entity.SysNotice;

import java.util.List;

/**
 * 公告 Service（用户端查看）
 */
public interface NoticeService {

    /**
     * 已发布公告列表（置顶优先，时间倒序）
     */
    List<SysNotice> listPublished();

    /**
     * 公告详情（仅已发布可见）
     */
    SysNotice detail(Long noticeId);
}
