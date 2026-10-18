package com.gxnu.library.controller;

import com.gxnu.library.annotation.RepeatSubmit;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.Result;
import com.gxnu.library.dto.BookingCreateDTO;
import com.gxnu.library.dto.BookingExtendDTO;
import com.gxnu.library.service.BookingService;
import com.gxnu.library.vo.BookingVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预约接口（提交 / 取消 / 签到 / 签退 / 续时 / 我的预约）
 */
@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * 提交预约（3秒内重复提交相同参数将被拦截）
     */
    @RepeatSubmit
    @PostMapping
    public Result<Long> create(@Valid @RequestBody BookingCreateDTO dto) {
        return Result.success(bookingService.create(dto));
    }

    /**
     * 取消预约
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable("id") Long id) {
        bookingService.cancel(id);
        return Result.success();
    }

    /**
     * 签到（开始时间起 30 分钟内）
     */
    @PutMapping("/{id}/checkin")
    public Result<Void> checkin(@PathVariable("id") Long id) {
        bookingService.checkin(id);
        return Result.success();
    }

    /**
     * 签退（进行中，即时释放座位）
     */
    @PutMapping("/{id}/checkout")
    public Result<Void> checkout(@PathVariable("id") Long id) {
        bookingService.checkout(id);
        return Result.success();
    }

    /**
     * 续时（进行中，延长结束时间）
     */
    @PutMapping("/{id}/extend")
    public Result<Void> extend(@PathVariable("id") Long id, @Valid @RequestBody BookingExtendDTO dto) {
        bookingService.extend(id, dto);
        return Result.success();
    }

    /**
     * 我的预约分页
     */
    @GetMapping("/my")
    public Result<PageResult<BookingVO>> my(@RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(bookingService.myPage(status, pageNum, pageSize));
    }

    /**
     * 我的预约详情
     */
    @GetMapping("/my/{id}")
    public Result<BookingVO> myDetail(@PathVariable("id") Long id) {
        return Result.success(bookingService.myDetail(id));
    }
}
