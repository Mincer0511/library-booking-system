package com.gxnu.library.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 每日订单数统计 VO
 */
@Data
public class DailyCountVO {

    /** 日期 */
    private LocalDate date;

    /** 订单数 */
    private Long count;
}
