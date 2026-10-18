package com.gxnu.library.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 座位"行×列"批量生成入参
 */
@Data
public class SeatBatchDTO {

    @NotNull(message = "行数不能为空")
    @Min(value = 1, message = "行数至少为1")
    @Max(value = 20, message = "行数最多20")
    private Integer rows;

    @NotNull(message = "列数不能为空")
    @Min(value = 1, message = "列数至少为1")
    @Max(value = 20, message = "列数最多20")
    private Integer cols;
}
