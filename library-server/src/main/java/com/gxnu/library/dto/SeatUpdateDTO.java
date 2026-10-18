package com.gxnu.library.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 座位修改入参（类型/坐标微调/插座/状态）
 */
@Data
public class SeatUpdateDTO {

    /** 座位类型：靠窗/带插座/普通 */
    private String seatType;

    @Min(value = 1, message = "X坐标至少为1")
    @Max(value = 30, message = "X坐标最多30")
    private Integer positionX;

    @Min(value = 1, message = "Y坐标至少为1")
    @Max(value = 30, message = "Y坐标最多30")
    private Integer positionY;

    /** 是否有插座：0否 1是 */
    @Min(0)
    @Max(1)
    private Integer hasSocket;

    /** 状态：0不可用 1可用 */
    @Min(0)
    @Max(1)
    private Integer status;
}
