package com.gxnu.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxnu.library.common.constant.OrderStatus;
import com.gxnu.library.common.result.ResultCode;
import com.gxnu.library.entity.BookingOrder;
import com.gxnu.library.entity.LibraryFloor;
import com.gxnu.library.entity.StudyRoom;
import com.gxnu.library.entity.StudySeat;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.BookingOrderMapper;
import com.gxnu.library.mapper.LibraryFloorMapper;
import com.gxnu.library.mapper.StudyRoomMapper;
import com.gxnu.library.mapper.StudySeatMapper;
import com.gxnu.library.service.RoomService;
import com.gxnu.library.utils.UserContext;
import com.gxnu.library.vo.RoomListVO;
import com.gxnu.library.vo.SeatMapVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自习室 Service 实现
 */
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final LibraryFloorMapper floorMapper;
    private final StudyRoomMapper roomMapper;
    private final StudySeatMapper seatMapper;
    private final BookingOrderMapper bookingMapper;

    @Override
    public List<LibraryFloor> listFloors() {
        return floorMapper.selectList(
                new LambdaQueryWrapper<LibraryFloor>().orderByAsc(LibraryFloor::getFloorNum));
    }

    @Override
    public List<RoomListVO> listRooms(Long floorId, String roomType, String keyword) {
        return roomMapper.selectRoomList(floorId, roomType, keyword);
    }

    @Override
    public StudyRoom detail(Long roomId) {
        StudyRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "自习室不存在");
        }
        return room;
    }

    @Override
    public SeatMapVO seatMap(Long roomId, LocalDate date) {
        StudyRoom room = detail(roomId);
        Long currentUserId = UserContext.currentUserId();

        // 座位列表（按坐标排序）
        List<StudySeat> seats = seatMapper.selectList(
                new LambdaQueryWrapper<StudySeat>()
                        .eq(StudySeat::getRoomId, roomId)
                        .orderByAsc(StudySeat::getPositionY)
                        .orderByAsc(StudySeat::getPositionX));

        // 当日有效预约（待签到/进行中），按座位分组
        List<BookingOrder> orders = bookingMapper.selectList(
                new LambdaQueryWrapper<BookingOrder>()
                        .eq(BookingOrder::getRoomId, roomId)
                        .eq(BookingOrder::getBookingDate, date)
                        .in(BookingOrder::getStatus, OrderStatus.WAIT_CHECKIN, OrderStatus.IN_USE));
        Map<Long, List<BookingOrder>> orderMap = orders.stream()
                .collect(Collectors.groupingBy(BookingOrder::getSeatId));

        // 组装座位项
        List<SeatMapVO.SeatItem> seatItems = new ArrayList<>();
        for (StudySeat seat : seats) {
            SeatMapVO.SeatItem item = new SeatMapVO.SeatItem();
            item.setSeatId(seat.getSeatId());
            item.setSeatCode(seat.getSeatCode());
            item.setSeatType(seat.getSeatType());
            item.setHasSocket(seat.getHasSocket());
            item.setPositionX(seat.getPositionX());
            item.setPositionY(seat.getPositionY());
            item.setUsable(seat.getStatus() == 1);

            List<SeatMapVO.SeatBooking> bookings = new ArrayList<>();
            for (BookingOrder order : orderMap.getOrDefault(seat.getSeatId(), List.of())) {
                SeatMapVO.SeatBooking booking = new SeatMapVO.SeatBooking();
                booking.setStartTime(order.getStartTime());
                booking.setEndTime(order.getEndTime());
                booking.setStatus(order.getStatus());
                booking.setMine(order.getUserId().equals(currentUserId));
                bookings.add(booking);
            }
            item.setBookings(bookings);
            seatItems.add(item);
        }

        // 楼层号
        LibraryFloor floor = floorMapper.selectById(room.getFloorId());

        SeatMapVO vo = new SeatMapVO();
        vo.setRoomId(room.getRoomId());
        vo.setRoomName(room.getRoomName());
        vo.setFloorNum(floor != null ? floor.getFloorNum() : null);
        vo.setOpenTime(room.getOpenTime());
        vo.setCloseTime(room.getCloseTime());
        vo.setRoomType(room.getRoomType());
        vo.setSeats(seatItems);
        return vo;
    }
}
