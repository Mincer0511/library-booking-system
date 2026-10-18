package com.gxnu.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 找回密码-发送验证码请求
 */
@Data
public class SendCodeDTO {

    /** 学号 */
    @NotBlank(message = "学号不能为空")
    private String username;

    /** 绑定手机号（用于身份核验） */
    @NotBlank(message = "手机号不能为空")
    private String phone;
}
