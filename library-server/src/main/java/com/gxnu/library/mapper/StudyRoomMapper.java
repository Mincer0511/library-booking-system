package com.gxnu.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxnu.library.entity.StudyRoom;
import com.gxnu.library.vo.RoomListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自习室 Mapper
 */
@Mapper
public interface StudyRoomMapper extends BaseMapper<StudyRoom> {

    /**
     * 自习室列表（含实时空余座位数）
     */
    List<RoomListVO> selectRoomList(@Param("floorId") Long floorId,
                                    @Param("roomType") String roomType,
                                    @Param("keyword") String keyword);
}
