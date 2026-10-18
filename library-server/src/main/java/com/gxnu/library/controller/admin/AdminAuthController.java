package com.gxnu.library.controller.admin;

import com.gxnu.library.annotation.RequireRole;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.common.result.Result;
import com.gxnu.library.service.UserService;
import com.gxnu.library.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端认证接口：登录复用 /api/auth/login，
 * 本控制器提供管理端专属入口（登录后校验管理员角色并获取信息）。
 */
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@RequireRole({RoleKey.LIBRARIAN, RoleKey.SUPER_ADMIN})
public class AdminAuthController {

    private final UserService userService;

    /**
     * 获取当前管理员信息（仅图书馆管理员/超级管理员可访问，学生访问返回 403）
     */
    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        return Result.success(userService.currentUser());
    }
}
