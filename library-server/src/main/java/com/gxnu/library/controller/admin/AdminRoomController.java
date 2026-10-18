package com.gxnu.library.controller.admin;

import com.gxnu.library.annotation.OperationLog;
import com.gxnu.library.annotation.RequireRole;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.Result;
import com.gxnu.library.dto.RoomSaveDTO;
import com.gxnu.library.dto.SeatBatchDTO;
import com.gxnu.library.dto.SeatUpdateDTO;
import com.gxnu.library.entity.LibraryFloor;
import com.gxnu.library.entity.StudyRoom;
import com.gxnu.library.entity.StudySeat;
import com.gxnu.library.service.AdminRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端基础数据：楼层 / 自习室 / 座位管理
 */
@RestController
@RequestMapping("/api/admin/room")
@RequiredArgsConstructor
@RequireRole({RoleKey.LIBRARIAN, RoleKey.SUPER_ADMIN})
public class AdminRoomController {

    private final AdminRoomService adminRoomService;

    /** 楼层列表 */
    @GetMapping("/floors")
    public Result<List<LibraryFloor>> floors() {
        return Result.success(adminRoomService.listFloors());
    }

    /** 自习室分页 */
    @GetMapping("/page")
    public Result<PageResult<StudyRoom>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(adminRoomService.roomPage(keyword, floorId, status, pageNum, pageSize));
    }

    @OperationLog(module = "自习室管理", content = "新增自习室")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody RoomSaveDTO dto) {
        adminRoomService.addRoom(dto);
        return Result.success();
    }

    @OperationLog(module = "自习室管理", content = "修改自习室")
    @PutMapping("/{roomId}")
    public Result<Void> update(@PathVariable Long roomId, @Valid @RequestBody RoomSaveDTO dto) {
        adminRoomService.updateRoom(roomId, dto);
        return Result.success();
    }

    @OperationLog(module = "自习室管理", content = "修改自习室开放状态")
    @PutMapping("/{roomId}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long roomId, @PathVariable Integer status) {
        adminRoomService.updateRoomStatus(roomId, status);
        return Result.success();
    }

    @OperationLog(module = "自习室管理", content = "删除自习室")
    @DeleteMapping("/{roomId}")
    public Result<Void> delete(@PathVariable Long roomId) {
        adminRoomService.deleteRoom(roomId);
        return Result.success();
    }

    /** 座位列表（平面图坐标排序） */
    @GetMapping("/{roomId}/seats")
    public Result<List<StudySeat>> seats(@PathVariable Long roomId) {
        return Result.success(adminRoomService.listSeats(roomId));
    }

    @OperationLog(module = "座位管理", content = "批量生成座位")
    @PostMapping("/{roomId}/seats/batch")
    public Result<Void> batchGenerate(@PathVariable Long roomId, @Valid @RequestBody SeatBatchDTO dto) {
        adminRoomService.batchGenerateSeats(roomId, dto);
        return Result.success();
    }

    @OperationLog(module = "座位管理", content = "修改座位")
    @PutMapping("/seat/{seatId}")
    public Result<Void> updateSeat(@PathVariable Long seatId, @Valid @RequestBody SeatUpdateDTO dto) {
        adminRoomService.updateSeat(seatId, dto);
        return Result.success();
    }

    @OperationLog(module = "座位管理", content = "删除座位")
    @DeleteMapping("/seat/{seatId}")
    public Result<Void> deleteSeat(@PathVariable Long seatId) {
        adminRoomService.deleteSeat(seatId);
        return Result.success();
    }
}
