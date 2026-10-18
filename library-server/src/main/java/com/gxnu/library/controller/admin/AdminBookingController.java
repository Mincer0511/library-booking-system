package com.gxnu.library.controller.admin;

import com.gxnu.library.annotation.OperationLog;
import com.gxnu.library.annotation.RequireRole;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.Result;
import com.gxnu.library.dto.BookingTimeDTO;
import com.gxnu.library.dto.ForceCancelDTO;
import com.gxnu.library.service.AdminBookingService;
import com.gxnu.library.utils.UserContext;
import com.gxnu.library.vo.AdminBookingVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端订单运营：组合筛选分页 / 强制取消 / 修改时间 / 备注
 */
@RestController
@RequestMapping("/api/admin/booking")
@RequiredArgsConstructor
@RequireRole({RoleKey.LIBRARIAN, RoleKey.SUPER_ADMIN})
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    @GetMapping("/page")
    public Result<PageResult<AdminBookingVO>> page(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String bookingDate,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(adminBookingService.page(userId, roomId, bookingDate, status, pageNum, pageSize));
    }

    @OperationLog(module = "订单管理", content = "强制取消订单")
    @PutMapping("/{orderId}/force-cancel")
    public Result<Void> forceCancel(@PathVariable Long orderId, @Valid @RequestBody ForceCancelDTO dto) {
        adminBookingService.forceCancel(orderId, dto.getReason(), UserContext.currentUserId());
        return Result.success();
    }

    @OperationLog(module = "订单管理", content = "修改订单时间")
    @PutMapping("/{orderId}/time")
    public Result<Void> updateTime(@PathVariable Long orderId, @Valid @RequestBody BookingTimeDTO dto) {
        adminBookingService.updateOrderTime(orderId, dto, UserContext.currentUserId());
        return Result.success();
    }

    @OperationLog(module = "订单管理", content = "修改订单备注")
    @PutMapping("/{orderId}/remark")
    public Result<Void> updateRemark(@PathVariable Long orderId, @RequestBody Map<String, String> body) {
        adminBookingService.updateRemark(orderId, body.get("remark"));
        return Result.success();
    }
}
