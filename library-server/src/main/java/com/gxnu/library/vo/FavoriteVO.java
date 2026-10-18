package com.gxnu.library.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 我的收藏（联查自习室/楼层）
 */
@Data
public class FavoriteVO {

    private Long favoriteId;
    private Long roomId;
    private String roomName;
    private Integer floorNum;
    private String roomType;
    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalDateTime createTime;
}
