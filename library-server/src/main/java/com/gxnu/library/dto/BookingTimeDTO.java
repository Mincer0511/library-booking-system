package com.gxnu.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理端修改订单时间入参
 */
@Data
public class BookingTimeDTO {

    /** 预约日期 yyyy-MM-dd */
    @NotBlank(message = "预约日期不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "日期格式为 yyyy-MM-dd")
    private String bookingDate;

    /** 开始时间 HH:mm */
    @NotBlank(message = "开始时间不能为空")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "时间格式为 HH:mm")
    private String startTime;

    /** 结束时间 HH:mm */
    @NotBlank(message = "结束时间不能为空")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "时间格式为 HH:mm")
    private String endTime;
}
