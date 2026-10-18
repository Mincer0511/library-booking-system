package com.gxnu.library.vo;

import lombok.Data;

/**
 * 热门开始时段统计 VO
 */
@Data
public class HotHourVO {

    /** 开始小时（0-23） */
    private Integer hour;

    /** 预约数 */
    private Long count;
}
