package com.gxnu.library.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnu.library.annotation.OperationLog;
import com.gxnu.library.annotation.RequireRole;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.Result;
import com.gxnu.library.common.result.ResultCode;
import com.gxnu.library.entity.SysNotice;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.SysNoticeMapper;
import com.gxnu.library.utils.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端公告管理：发布 / 编辑 / 删除 / 置顶 / 上下架
 */
@RestController
@RequestMapping("/api/admin/notice")
@RequiredArgsConstructor
@RequireRole({RoleKey.LIBRARIAN, RoleKey.SUPER_ADMIN})
public class AdminNoticeController {

    private final SysNoticeMapper noticeMapper;

    @Data
    public static class NoticeSaveDTO {
        @NotBlank(message = "标题不能为空")
        @Size(max = 100, message = "标题不能超过100字")
        private String title;

        @NotBlank(message = "内容不能为空")
        private String content;

        @NotNull(message = "是否置顶不能为空")
        @Min(value = 0, message = "置顶值非法")
        @Max(value = 1, message = "置顶值非法")
        private Integer isTop;

        @NotNull(message = "状态不能为空")
        @Min(value = 0, message = "状态值非法")
        @Max(value = 1, message = "状态值非法")
        private Integer status;
    }

    /** 公告分页：标题模糊 + 状态 */
    @GetMapping("/page")
    public Result<PageResult<SysNotice>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        Page<SysNotice> page = noticeMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysNotice>()
                        .like(StringUtils.hasText(keyword), SysNotice::getTitle, keyword)
                        .eq(status != null, SysNotice::getStatus, status)
                        .orderByDesc(SysNotice::getIsTop)
                        .orderByDesc(SysNotice::getCreateTime));
        return Result.success(PageResult.of(page));
    }

    @OperationLog(module = "公告管理", content = "发布公告")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody NoticeSaveDTO dto) {
        SysNotice notice = new SysNotice();
        applySave(notice, dto);
        notice.setCreateBy(UserContext.currentUserId());
        noticeMapper.insert(notice);
        return Result.success();
    }

    @OperationLog(module = "公告管理", content = "编辑公告")
    @PutMapping("/{noticeId}")
    public Result<Void> update(@PathVariable Long noticeId, @Valid @RequestBody NoticeSaveDTO dto) {
        SysNotice notice = requireNotice(noticeId);
        applySave(notice, dto);
        noticeMapper.updateById(notice);
        return Result.success();
    }

    @OperationLog(module = "公告管理", content = "删除公告")
    @DeleteMapping("/{noticeId}")
    public Result<Void> delete(@PathVariable Long noticeId) {
        requireNotice(noticeId);
        noticeMapper.deleteById(noticeId);
        return Result.success();
    }

    @OperationLog(module = "公告管理", content = "设置公告置顶")
    @PutMapping("/{noticeId}/top/{isTop}")
    public Result<Void> updateTop(@PathVariable Long noticeId, @PathVariable Integer isTop) {
        requireNotice(noticeId);
        noticeMapper.update(null, new LambdaUpdateWrapper<SysNotice>()
                .eq(SysNotice::getNoticeId, noticeId)
                .set(SysNotice::getIsTop, isTop));
        return Result.success();
    }

    @OperationLog(module = "公告管理", content = "设置公告上下架")
    @PutMapping("/{noticeId}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long noticeId, @PathVariable Integer status) {
        requireNotice(noticeId);
        noticeMapper.update(null, new LambdaUpdateWrapper<SysNotice>()
                .eq(SysNotice::getNoticeId, noticeId)
                .set(SysNotice::getStatus, status));
        return Result.success();
    }

    // ===== 私有方法 =====

    private SysNotice requireNotice(Long noticeId) {
        SysNotice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "公告不存在");
        }
        return notice;
    }

    private void applySave(SysNotice notice, NoticeSaveDTO dto) {
        notice.setTitle(dto.getTitle().trim());
        notice.setContent(dto.getContent());
        notice.setIsTop(dto.getIsTop());
        notice.setStatus(dto.getStatus());
    }
}
