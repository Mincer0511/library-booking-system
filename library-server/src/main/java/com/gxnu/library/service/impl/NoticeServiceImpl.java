package com.gxnu.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxnu.library.common.result.ResultCode;
import com.gxnu.library.entity.SysNotice;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.SysNoticeMapper;
import com.gxnu.library.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告 Service 实现
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final SysNoticeMapper noticeMapper;

    @Override
    public List<SysNotice> listPublished() {
        return noticeMapper.selectList(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getStatus, 1)
                .orderByDesc(SysNotice::getIsTop)
                .orderByDesc(SysNotice::getCreateTime));
    }

    @Override
    public SysNotice detail(Long noticeId) {
        SysNotice notice = noticeMapper.selectById(noticeId);
        if (notice == null || notice.getStatus() != 1) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "公告不存在或已下架");
        }
        return notice;
    }
}
