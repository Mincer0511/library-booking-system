package com.gxnu.library.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理端手动添加违规入参
 */
@Data
public class ViolationAddDTO {

    @NotNull(message = "用户不能为空")
    private Long userId;

    /** 关联订单（可空） */
    private Long orderId;

    @NotBlank(message = "违规类型不能为空")
    @Size(max = 20, message = "类型不能超过20字")
    private String violationType;

    @Size(max = 255, message = "描述不能超过255字")
    private String violationDesc;

    @NotNull(message = "扣分不能为空")
    @Min(value = 1, message = "扣分至少1分")
    @Max(value = 100, message = "扣分最多100分")
    private Integer deductScore;
}
