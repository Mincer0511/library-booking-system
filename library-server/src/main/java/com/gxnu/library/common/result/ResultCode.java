package com.gxnu.library.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限操作"),
    PARAM_ERROR(400, "参数校验失败"),
    ERROR(500, "系统繁忙，请稍后再试"),

    // 业务状态码 1xxx
    USER_DISABLED(1001, "账号处于预约禁用期"),
    BOOKING_CONFLICT(1002, "该座位在所选时段已被预约"),
    BOOKING_LIMIT(1003, "超出预约规则限制"),
    BOOKING_STATE_ERROR(1004, "订单状态不允许该操作"),
    DATA_NOT_FOUND(1005, "数据不存在或已被删除");

    private final Integer code;
    private final String msg;
}
