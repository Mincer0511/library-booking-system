package com.gxnu.library.controller;

import com.gxnu.library.common.result.Result;
import com.gxnu.library.dto.LoginDTO;
import com.gxnu.library.dto.RegisterDTO;
import com.gxnu.library.dto.ResetPasswordDTO;
import com.gxnu.library.dto.SendCodeDTO;
import com.gxnu.library.service.UserService;
import com.gxnu.library.vo.LoginVO;
import com.gxnu.library.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证接口：注册 / 登录 / 找回密码 / 当前用户
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 学生注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    /**
     * 登录（学号 + 密码）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        return Result.success(userService.currentUser());
    }

    /**
     * 找回密码-发送模拟验证码（学号+手机号核验，返回值回显验证码）
     */
    @PostMapping("/forget/send-code")
    public Result<Map<String, Object>> sendForgetCode(@Valid @RequestBody SendCodeDTO dto) {
        return Result.success(userService.sendForgetCode(dto));
    }

    /**
     * 找回密码-验证码校验 + 重置密码
     */
    @PostMapping("/forget/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPasswordByCode(dto);
        return Result.success();
    }
}
