package com.gxnu.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 自习室新增/修改入参
 */
@Data
public class RoomSaveDTO {

    @NotNull(message = "所属楼层不能为空")
    private Long floorId;

    @NotBlank(message = "自习室名称不能为空")
    @Size(max = 50, message = "名称不能超过50字")
    private String roomName;

    /** 开放时间 HH:mm */
    @NotBlank(message = "开放时间不能为空")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "时间格式为 HH:mm")
    private String openTime;

    /** 关闭时间 HH:mm */
    @NotBlank(message = "关闭时间不能为空")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "时间格式为 HH:mm")
    private String closeTime;

    /** 类型：普通/静音/研讨 */
    @NotBlank(message = "类型不能为空")
    private String roomType;

    @Size(max = 255, message = "简介不能超过255字")
    private String description;

    /** 状态：0关闭 1开放 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
