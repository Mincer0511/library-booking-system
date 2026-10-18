package com.gxnu.library.service;

import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.dto.RoomSaveDTO;
import com.gxnu.library.dto.SeatBatchDTO;
import com.gxnu.library.dto.SeatUpdateDTO;
import com.gxnu.library.entity.LibraryFloor;
import com.gxnu.library.entity.StudyRoom;
import com.gxnu.library.entity.StudySeat;

import java.util.List;

/**
 * 管理端楼层/自习室/座位管理 Service
 */
public interface AdminRoomService {

    /** 楼层列表（按楼层号升序） */
    List<LibraryFloor> listFloors();

    /** 自习室分页：关键词（名称模糊）/ 楼层 / 状态 */
    PageResult<StudyRoom> roomPage(String keyword, Long floorId, Integer status,
                                   long pageNum, long pageSize);

    /** 新增自习室 */
    void addRoom(RoomSaveDTO dto);

    /** 修改自习室 */
    void updateRoom(Long roomId, RoomSaveDTO dto);

    /** 开放/关闭自习室 */
    void updateRoomStatus(Long roomId, Integer status);

    /** 删除自习室（存在未完成订单时拒绝） */
    void deleteRoom(Long roomId);

    /** 座位列表（按平面图坐标排序） */
    List<StudySeat> listSeats(Long roomId);

    /** 按"行×列"批量重新生成座位（存在未完成订单时拒绝，原座位将被清空） */
    void batchGenerateSeats(Long roomId, SeatBatchDTO dto);

    /** 修改座位（类型/坐标/插座/状态） */
    void updateSeat(Long seatId, SeatUpdateDTO dto);

    /** 删除座位（被未完成订单引用时拒绝） */
    void deleteSeat(Long seatId);
}
