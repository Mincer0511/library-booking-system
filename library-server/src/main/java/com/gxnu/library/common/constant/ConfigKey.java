package com.gxnu.library.common.constant;

/**
 * 系统配置键常量（对应 sys_config 表）
 */
public class ConfigKey {

    /** 每日最大预约单数 */
    public static final String BOOKING_MAX_PER_DAY = "booking.max_per_day";
    /** 单次最长时长（小时） */
    public static final String BOOKING_MAX_HOURS = "booking.max_hours";
    /** 可提前预约天数 */
    public static final String BOOKING_ADVANCE_DAYS = "booking.advance_days";
    /** 超时未签到判爽约分钟数 */
    public static final String CHECKIN_LATE_MINUTES = "checkin.late_minutes";
    /** 初始信用分 */
    public static final String CREDIT_INIT_SCORE = "credit.init_score";
    /** 爽约单次扣分 */
    public static final String CREDIT_VIOLATION_DEDUCT = "credit.violation_deduct";
    /** 禁用阈值 */
    public static final String CREDIT_DISABLE_THRESHOLD = "credit.disable_threshold";
    /** 禁用天数 */
    public static final String CREDIT_DISABLE_DAYS = "credit.disable_days";
    /** 解禁后恢复分数 */
    public static final String CREDIT_RESTORE_SCORE = "credit.restore_score";

    private ConfigKey() {
    }
}
