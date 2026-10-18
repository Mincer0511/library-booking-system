package com.gxnu.library.common.constant;

/**
 * 预约订单状态常量
 */
public class OrderStatus {

    /** 待签到 */
    public static final int WAIT_CHECKIN = 0;
    /** 进行中 */
    public static final int IN_USE = 1;
    /** 已完成 */
    public static final int FINISHED = 2;
    /** 已取消 */
    public static final int CANCELED = 3;
    /** 已爽约 */
    public static final int NO_SHOW = 4;

    private OrderStatus() {
    }
}
