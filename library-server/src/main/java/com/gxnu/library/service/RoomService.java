package com.gxnu.library.service;

import com.gxnu.library.entity.LibraryFloor;
import com.gxnu.library.entity.StudyRoom;
import com.gxnu.library.vo.RoomListVO;
import com.gxnu.library.vo.SeatMapVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 自习室 Service
 */
public interface RoomService {

    /**
     * 楼层列表（1-5层）
     */
    List<LibraryFloor> listFloors();

    /**
     * 自习室列表（含实时空余座位）
     */
    List<RoomListVO> listRooms(Long floorId, String roomType, String keyword);

    /**
     * 自习室详情
     */
    StudyRoom detail(Long roomId);

    /**
     * 座位平面图（含指定日期各座位预约状态）
     */
    SeatMapVO seatMap(Long roomId, LocalDate date);
}
