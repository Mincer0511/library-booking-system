package com.gxnu.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 超管新增用户入参
 */
@Data
public class UserSaveDTO {

    /** 学号/登录账号 */
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]{4,20}$", message = "账号为4-20位字母或数字")
    private String username;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 20, message = "姓名不能超过20字")
    private String name;

    @Size(max = 20, message = "昵称不能超过20字")
    private String nickname;

    /** 手机号（可空） */
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @NotNull(message = "角色不能为空")
    private Long roleId;

    /** 初始密码（空则默认 123456） */
    @Size(min = 6, max = 20, message = "密码为6-20位")
    private String password;
}
