package com.gxnu.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.dto.RoomSaveDTO;
import com.gxnu.library.dto.SeatBatchDTO;
import com.gxnu.library.dto.SeatUpdateDTO;
import com.gxnu.library.entity.LibraryFloor;
import com.gxnu.library.entity.StudyRoom;
import com.gxnu.library.entity.StudySeat;
import com.gxnu.library.entity.UserFavorite;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.BookingOrderMapper;
import com.gxnu.library.mapper.LibraryFloorMapper;
import com.gxnu.library.mapper.StudyRoomMapper;
import com.gxnu.library.mapper.StudySeatMapper;
import com.gxnu.library.mapper.UserFavoriteMapper;
import com.gxnu.library.service.AdminRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 管理端楼层/自习室/座位管理实现
 */
@Service
@RequiredArgsConstructor
public class AdminRoomServiceImpl implements AdminRoomService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final LibraryFloorMapper floorMapper;
    private final StudyRoomMapper roomMapper;
    private final StudySeatMapper seatMapper;
    private final BookingOrderMapper bookingMapper;
    private final UserFavoriteMapper favoriteMapper;

    @Override
    public List<LibraryFloor> listFloors() {
        return floorMapper.selectList(
                new LambdaQueryWrapper<LibraryFloor>().orderByAsc(LibraryFloor::getFloorNum));
    }

    @Override
    public PageResult<StudyRoom> roomPage(String keyword, Long floorId, Integer status,
                                          long pageNum, long pageSize) {
        Page<StudyRoom> page = roomMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<StudyRoom>()
                        .like(StringUtils.hasText(keyword), StudyRoom::getRoomName, keyword)
                        .eq(floorId != null, StudyRoom::getFloorId, floorId)
                        .eq(status != null, StudyRoom::getStatus, status)
                        .orderByAsc(StudyRoom::getFloorId)
                        .orderByAsc(StudyRoom::getRoomId));
        return PageResult.of(page);
    }

    @Override
    public void addRoom(RoomSaveDTO dto) {
        checkFloor(dto.getFloorId());
        StudyRoom room = new StudyRoom();
        applySave(room, dto);
        room.setSeatCount(0);
        roomMapper.insert(room);
    }

    @Override
    public void updateRoom(Long roomId, RoomSaveDTO dto) {
        StudyRoom room = requireRoom(roomId);
        checkFloor(dto.getFloorId());
        applySave(room, dto);
        roomMapper.updateById(room);
    }

    @Override
    public void updateRoomStatus(Long roomId, Integer status) {
        requireRoom(roomId);
        roomMapper.update(null, new LambdaUpdateWrapper<StudyRoom>()
                .eq(StudyRoom::getRoomId, roomId)
                .set(StudyRoom::getStatus, status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Long roomId) {
        requireRoom(roomId);
        checkNoActiveOrder(roomId, "该自习室存在未完成订单，不能删除");
        seatMapper.delete(new LambdaQueryWrapper<StudySeat>().eq(StudySeat::getRoomId, roomId));
        favoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>().eq(UserFavorite::getRoomId, roomId));
        roomMapper.deleteById(roomId);
    }

    @Override
    public List<StudySeat> listSeats(Long roomId) {
        requireRoom(roomId);
        return seatMapper.selectList(new LambdaQueryWrapper<StudySeat>()
                .eq(StudySeat::getRoomId, roomId)
                .orderByAsc(StudySeat::getPositionY)
                .orderByAsc(StudySeat::getPositionX));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchGenerateSeats(Long roomId, SeatBatchDTO dto) {
        requireRoom(roomId);
        checkNoActiveOrder(roomId, "该自习室存在未完成订单，不能重新生成座位");

        // 清空原座位后按行×列重新生成：两侧靠窗，首末排带插座（与初始数据规则一致）
        seatMapper.delete(new LambdaQueryWrapper<StudySeat>().eq(StudySeat::getRoomId, roomId));
        String[] rowLetters = "ABCDEFGHIJKLMNOPQRSTUV".split("");
        int rows = dto.getRows();
        int cols = dto.getCols();
        for (int y = 1; y <= rows; y++) {
            for (int x = 1; x <= cols; x++) {
                StudySeat seat = new StudySeat();
                seat.setRoomId(roomId);
                seat.setSeatCode(rowLetters[y - 1] + String.format("%02d", x));
                boolean window = (x == 1 || x == cols);
                boolean socket = (y == 1 || y == rows);
                seat.setSeatType(window ? "靠窗" : (socket ? "带插座" : "普通"));
                seat.setHasSocket(socket ? 1 : 0);
                seat.setPositionX(x);
                seat.setPositionY(y);
                seat.setStatus(1);
                seatMapper.insert(seat);
            }
        }
        int count = rows * cols;
        roomMapper.update(null, new LambdaUpdateWrapper<StudyRoom>()
                .eq(StudyRoom::getRoomId, roomId)
                .set(StudyRoom::getSeatCount, count));
    }

    @Override
    public void updateSeat(Long seatId, SeatUpdateDTO dto) {
        StudySeat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            throw new BusinessException("座位不存在");
        }
        if (StringUtils.hasText(dto.getSeatType())) {
            seat.setSeatType(dto.getSeatType());
        }
        if (dto.getPositionX() != null) {
            seat.setPositionX(dto.getPositionX());
        }
        if (dto.getPositionY() != null) {
            seat.setPositionY(dto.getPositionY());
        }
        if (dto.getHasSocket() != null) {
            seat.setHasSocket(dto.getHasSocket());
        }
        if (dto.getStatus() != null) {
            seat.setStatus(dto.getStatus());
        }
        seatMapper.updateById(seat);
    }

    @Override
    public void deleteSeat(Long seatId) {
        StudySeat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            throw new BusinessException("座位不存在");
        }
        Long active = bookingMapper.selectCount(new LambdaQueryWrapper<com.gxnu.library.entity.BookingOrder>()
                .eq(com.gxnu.library.entity.BookingOrder::getSeatId, seatId)
                .in(com.gxnu.library.entity.BookingOrder::getStatus, 0, 1));
        if (active > 0) {
            throw new BusinessException("该座位存在未完成订单，不能删除");
        }
        seatMapper.deleteById(seatId);
        roomMapper.update(null, new LambdaUpdateWrapper<StudyRoom>()
                .eq(StudyRoom::getRoomId, seat.getRoomId())
                .setSql("seat_count = seat_count - 1"));
    }

    // ===== 私有方法 =====

    private StudyRoom requireRoom(Long roomId) {
        StudyRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }
        return room;
    }

    private void checkFloor(Long floorId) {
        if (floorMapper.selectById(floorId) == null) {
            throw new BusinessException("所属楼层不存在");
        }
    }

    /** 存在待签到/进行中订单即视为有未完成订单 */
    private void checkNoActiveOrder(Long roomId, String message) {
        Long active = bookingMapper.selectCount(
                new LambdaQueryWrapper<com.gxnu.library.entity.BookingOrder>()
                        .eq(com.gxnu.library.entity.BookingOrder::getRoomId, roomId)
                        .in(com.gxnu.library.entity.BookingOrder::getStatus, 0, 1));
        if (active > 0) {
            throw new BusinessException(message);
        }
    }

    private void applySave(StudyRoom room, RoomSaveDTO dto) {
        room.setFloorId(dto.getFloorId());
        room.setRoomName(dto.getRoomName().trim());
        room.setOpenTime(LocalTime.parse(dto.getOpenTime(), TIME_FMT));
        room.setCloseTime(LocalTime.parse(dto.getCloseTime(), TIME_FMT));
        if (!room.getOpenTime().isBefore(room.getCloseTime())) {
            throw new BusinessException("开放时间必须早于关闭时间");
        }
        room.setRoomType(dto.getRoomType());
        room.setDescription(dto.getDescription());
        room.setStatus(dto.getStatus());
    }
}
