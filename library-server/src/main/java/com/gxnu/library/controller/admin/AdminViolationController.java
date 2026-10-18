package com.gxnu.library.controller.admin;

import com.gxnu.library.annotation.OperationLog;
import com.gxnu.library.annotation.RepeatSubmit;
import com.gxnu.library.annotation.RequireRole;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.Result;
import com.gxnu.library.dto.ViolationAddDTO;
import com.gxnu.library.service.AdminViolationService;
import com.gxnu.library.utils.UserContext;
import com.gxnu.library.vo.AdminViolationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端违规管理：列表 / 手动添加（联动扣分+禁用判定）/ 撤销（联动恢复）
 */
@RestController
@RequestMapping("/api/admin/violation")
@RequiredArgsConstructor
@RequireRole({RoleKey.LIBRARIAN, RoleKey.SUPER_ADMIN})
public class AdminViolationController {

    private final AdminViolationService adminViolationService;

    @GetMapping("/page")
    public Result<PageResult<AdminViolationVO>> page(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String violationType,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(adminViolationService.page(userId, violationType, pageNum, pageSize));
    }

    @OperationLog(module = "违规管理", content = "手动添加违规")
    @RepeatSubmit
    @PostMapping
    public Result<Void> add(@Valid @RequestBody ViolationAddDTO dto) {
        adminViolationService.add(dto, UserContext.currentUserId());
        return Result.success();
    }

    @OperationLog(module = "违规管理", content = "撤销违规记录")
    @PutMapping("/{violationId}/revoke")
    public Result<Void> revoke(@PathVariable Long violationId) {
        adminViolationService.revoke(violationId, UserContext.currentUserId());
        return Result.success();
    }
}
