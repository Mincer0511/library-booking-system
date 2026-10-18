package com.gxnu.library.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnu.library.annotation.RequireRole;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.Result;
import com.gxnu.library.entity.SysOperationLog;
import com.gxnu.library.mapper.SysOperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志查询（管理端）：模块模糊筛选 + 操作人精确筛选
 */
@RestController
@RequestMapping("/api/admin/log")
@RequiredArgsConstructor
@RequireRole({RoleKey.LIBRARIAN, RoleKey.SUPER_ADMIN})
public class AdminLogController {

    private final SysOperationLogMapper logMapper;

    @GetMapping("/page")
    public Result<PageResult<SysOperationLog>> page(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        Page<SysOperationLog> page = logMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysOperationLog>()
                        .like(StringUtils.hasText(module), SysOperationLog::getModule, module)
                        .eq(userId != null, SysOperationLog::getUserId, userId)
                        .orderByDesc(SysOperationLog::getCreateTime));
        return Result.success(PageResult.of(page));
    }
}
