package com.gxnu.library.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

/**
 * 预约提交入参
 */
@Data
public class BookingCreateDTO {

    @NotNull(message = "座位不能为空")
    private Long seatId;

    @NotNull(message = "预约日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    /** 开始时间，格式 HH:mm（30分钟粒度） */
    @NotBlank(message = "开始时间不能为空")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[03]0$", message = "开始时间须为HH:mm格式且按30分钟对齐")
    private String startTime;

    /** 结束时间，格式 HH:mm（30分钟粒度） */
    @NotBlank(message = "结束时间不能为空")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[03]0$", message = "结束时间须为HH:mm格式且按30分钟对齐")
    private String endTime;
}
