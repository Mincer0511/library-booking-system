package com.gxnu.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 续时请求
 */
@Data
public class BookingExtendDTO {

    /** 新的结束时间（HH:mm，30 分钟粒度） */
    @NotBlank(message = "请选择新的结束时间")
    @Pattern(regexp = "^([01]\\d|2[0-3]):(00|30)$", message = "结束时间须为30分钟粒度（HH:mm）")
    private String newEndTime;
}
