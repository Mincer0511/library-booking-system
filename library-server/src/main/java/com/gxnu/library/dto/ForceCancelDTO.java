package com.gxnu.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理端强制取消入参
 */
@Data
public class ForceCancelDTO {

    @NotBlank(message = "取消原因不能为空")
    @Size(max = 200, message = "原因不能超过200字")
    private String reason;
}
