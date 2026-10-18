package com.gxnu.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 超管修改用户入参（账号不可改）
 */
@Data
public class UserUpdateDTO {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 20, message = "姓名不能超过20字")
    private String name;

    @Size(max = 20, message = "昵称不能超过20字")
    private String nickname;

    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "角色不能为空")
    private Long roleId;

    /** 账号状态：0禁用 1正常 */
    private Integer status;
}
