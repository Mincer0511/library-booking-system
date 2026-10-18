package com.gxnu.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxnu.library.common.result.ResultCode;
import com.gxnu.library.entity.LibraryFloor;
import com.gxnu.library.entity.StudyRoom;
import com.gxnu.library.entity.UserFavorite;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.LibraryFloorMapper;
import com.gxnu.library.mapper.StudyRoomMapper;
import com.gxnu.library.mapper.UserFavoriteMapper;
import com.gxnu.library.service.FavoriteService;
import com.gxnu.library.utils.UserContext;
import com.gxnu.library.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏 Service 实现
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserFavoriteMapper favoriteMapper;
    private final StudyRoomMapper roomMapper;
    private final LibraryFloorMapper floorMapper;

    @Override
    public void add(Long roomId) {
        Long userId = UserContext.currentUserId();
        StudyRoom room = roomMapper.selectById(roomId);
        if (room == null || room.getStatus() != 1) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "自习室不存在或未开放");
        }
        // 幂等：已收藏直接返回（uk_user_room 唯一约束兜底）
        Long exists = favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getRoomId, roomId));
        if (exists > 0) {
            return;
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setRoomId(roomId);
        favoriteMapper.insert(favorite);
    }

    @Override
    public void remove(Long roomId) {
        Long userId = UserContext.currentUserId();
        favoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getRoomId, roomId));
    }

    @Override
    public List<FavoriteVO> myList() {
        Long userId = UserContext.currentUserId();
        List<UserFavorite> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .orderByDesc(UserFavorite::getCreateTime));
        if (favorites.isEmpty()) {
            return List.of();
        }
        List<Long> roomIds = favorites.stream().map(UserFavorite::getRoomId).toList();
        Map<Long, StudyRoom> roomMap = roomMapper.selectBatchIds(roomIds).stream()
                .collect(Collectors.toMap(StudyRoom::getRoomId, r -> r));
        Map<Long, LibraryFloor> floorMap = floorMapper.selectBatchIds(
                        roomMap.values().stream().map(StudyRoom::getFloorId).distinct().toList())
                .stream().collect(Collectors.toMap(LibraryFloor::getFloorId, f -> f));

        return favorites.stream().map(f -> {
            FavoriteVO vo = new FavoriteVO();
            vo.setFavoriteId(f.getFavoriteId());
            vo.setRoomId(f.getRoomId());
            vo.setCreateTime(f.getCreateTime());
            StudyRoom room = roomMap.get(f.getRoomId());
            if (room != null) {
                vo.setRoomName(room.getRoomName());
                vo.setRoomType(room.getRoomType());
                vo.setOpenTime(room.getOpenTime());
                vo.setCloseTime(room.getCloseTime());
                LibraryFloor floor = floorMap.get(room.getFloorId());
                vo.setFloorNum(floor != null ? floor.getFloorNum() : null);
            }
            return vo;
        }).toList();
    }

    @Override
    public List<Long> myRoomIds() {
        Long userId = UserContext.currentUserId();
        return favoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .select(UserFavorite::getRoomId))
                .stream().map(UserFavorite::getRoomId).toList();
    }
}
