package com.gxnu.library.service;

import com.gxnu.library.vo.FavoriteVO;

import java.util.List;

/**
 * 收藏 Service
 */
public interface FavoriteService {

    /**
     * 收藏自习室（幂等：已收藏不报错）
     */
    void add(Long roomId);

    /**
     * 取消收藏
     */
    void remove(Long roomId);

    /**
     * 我的收藏列表（联查自习室信息，时间倒序）
     */
    List<FavoriteVO> myList();

    /**
     * 我已收藏的自习室ID集合（房间卡片收藏状态联动）
     */
    List<Long> myRoomIds();
}
