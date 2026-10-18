package com.gxnu.library.controller;

import com.gxnu.library.common.result.Result;
import com.gxnu.library.dto.PasswordUpdateDTO;
import com.gxnu.library.dto.ProfileUpdateDTO;
import com.gxnu.library.entity.ViolationRecord;
import com.gxnu.library.service.CreditService;
import com.gxnu.library.service.UserService;
import com.gxnu.library.vo.CreditVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户中心接口：信用面板 / 违规记录 / 个人资料 / 头像 / 密码
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final CreditService creditService;
    private final UserService userService;

    /**
     * 我的信用面板（分数、禁用状态、解禁时间、近期扣分记录）
     */
    @GetMapping("/credit")
    public Result<CreditVO> credit() {
        return Result.success(creditService.myPanel());
    }

    /**
     * 我的违规记录（时间倒序）
     */
    @GetMapping("/violation")
    public Result<List<ViolationRecord>> violation() {
        return Result.success(creditService.myViolations());
    }

    /**
     * 修改个人资料（昵称/手机号）
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        userService.updateProfile(dto);
        return Result.success();
    }

    /**
     * 修改登录密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        userService.updatePassword(dto);
        return Result.success();
    }

    /**
     * 上传头像（jpg/png/webp，≤2MB），返回可访问路径
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return Result.success(userService.updateAvatar(file));
    }
}
