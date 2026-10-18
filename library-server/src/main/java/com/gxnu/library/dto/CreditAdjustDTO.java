package com.gxnu.library.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 超管信用分调整入参
 */
@Data
public class CreditAdjustDTO {

    @NotNull(message = "分数不能为空")
    @Min(value = 0, message = "分数不能低于0")
    @Max(value = 100, message = "分数不能超过100")
    private Integer score;

    @NotBlank(message = "调整原因不能为空")
    @Size(max = 100, message = "原因不能超过100字")
    private String reason;
}
