package com.gxnu.library.controller;

import com.gxnu.library.common.result.Result;
import com.gxnu.library.entity.LibraryFloor;
import com.gxnu.library.entity.StudyRoom;
import com.gxnu.library.service.RoomService;
import com.gxnu.library.vo.RoomListVO;
import com.gxnu.library.vo.SeatMapVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 自习室浏览接口
 */
@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * 楼层列表（1-5层）
     */
    @GetMapping("/floors")
    public Result<List<LibraryFloor>> floors() {
        return Result.success(roomService.listFloors());
    }

    /**
     * 自习室列表（含实时空余座位）
     */
    @GetMapping("/list")
    public Result<List<RoomListVO>> list(@RequestParam(required = false) Long floorId,
                                         @RequestParam(required = false) String roomType,
                                         @RequestParam(required = false) String keyword) {
        return Result.success(roomService.listRooms(floorId, roomType, keyword));
    }

    /**
     * 自习室详情
     */
    @GetMapping("/{id}")
    public Result<StudyRoom> detail(@PathVariable("id") Long id) {
        return Result.success(roomService.detail(id));
    }

    /**
     * 座位平面图（默认当天）
     */
    @GetMapping("/{id}/seat-map")
    public Result<SeatMapVO> seatMap(@PathVariable("id") Long id,
                                     @RequestParam(required = false)
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(roomService.seatMap(id, date == null ? LocalDate.now() : date));
    }
}
