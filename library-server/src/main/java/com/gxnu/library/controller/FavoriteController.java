package com.gxnu.library.controller;

import com.gxnu.library.common.result.Result;
import com.gxnu.library.service.FavoriteService;
import com.gxnu.library.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 收藏接口
 */
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 收藏自习室
     */
    @PostMapping("/{roomId}")
    public Result<Void> add(@PathVariable("roomId") Long roomId) {
        favoriteService.add(roomId);
        return Result.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{roomId}")
    public Result<Void> remove(@PathVariable("roomId") Long roomId) {
        favoriteService.remove(roomId);
        return Result.success();
    }

    /**
     * 我的收藏列表
     */
    @GetMapping("/list")
    public Result<List<FavoriteVO>> list() {
        return Result.success(favoriteService.myList());
    }

    /**
     * 我已收藏的自习室ID集合
     */
    @GetMapping("/ids")
    public Result<List<Long>> ids() {
        return Result.success(favoriteService.myRoomIds());
    }
}
