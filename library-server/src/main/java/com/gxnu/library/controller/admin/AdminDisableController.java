package com.gxnu.library.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnu.library.annotation.OperationLog;
import com.gxnu.library.annotation.RequireRole;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.Result;
import com.gxnu.library.common.result.ResultCode;
import com.gxnu.library.dto.CreditAdjustDTO;
import com.gxnu.library.entity.DisableRecord;
import com.gxnu.library.entity.SysMessage;
import com.gxnu.library.entity.SysUser;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.DisableRecordMapper;
import com.gxnu.library.mapper.SysMessageMapper;
import com.gxnu.library.mapper.SysUserMapper;
import com.gxnu.library.service.CreditService;
import com.gxnu.library.utils.UserContext;
import com.gxnu.library.vo.DisableRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 超管专属：禁用记录列表 / 手动提前解禁 / 信用分调整
 */
@RestController
@RequestMapping("/api/admin/disable")
@RequiredArgsConstructor
@RequireRole(RoleKey.SUPER_ADMIN)
public class AdminDisableController {

    private final DisableRecordMapper disableMapper;
    private final SysUserMapper userMapper;
    private final SysMessageMapper messageMapper;
    private final CreditService creditService;

    /** 禁用记录分页（联查用户） */
    @GetMapping("/page")
    public Result<PageResult<DisableRecordVO>> page(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer isLifted,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        Page<DisableRecord> page = disableMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<DisableRecord>()
                        .eq(userId != null, DisableRecord::getUserId, userId)
                        .eq(isLifted != null, DisableRecord::getIsLifted, isLifted)
                        .orderByDesc(DisableRecord::getCreateTime));

        Map<Long, SysUser> userMap = page.getRecords().isEmpty() ? Map.of()
                : userMapper.selectBatchIds(page.getRecords().stream()
                        .map(DisableRecord::getUserId).distinct().toList()).stream()
                        .collect(Collectors.toMap(SysUser::getUserId, Function.identity()));

        List<DisableRecordVO> list = page.getRecords().stream().map(record -> {
            DisableRecordVO vo = new DisableRecordVO();
            vo.setDisableId(record.getDisableId());
            vo.setUserId(record.getUserId());
            vo.setDisableReason(record.getDisableReason());
            vo.setStartTime(record.getStartTime());
            vo.setEndTime(record.getEndTime());
            vo.setIsLifted(record.getIsLifted());
            vo.setLiftBy(record.getLiftBy());
            vo.setLiftTime(record.getLiftTime());
            vo.setCreateTime(record.getCreateTime());
            SysUser user = userMap.get(record.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setRealName(user.getName());
            }
            return vo;
        }).toList();

        PageResult<DisableRecordVO> result = new PageResult<>();
        result.setList(list);
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        return Result.success(result);
    }

    /** 手动提前解禁（复用 liftDisable：恢复信用分 + 记录留痕 + 通知） */
    @OperationLog(module = "禁用管理", content = "手动提前解禁")
    @PutMapping("/lift/{userId}")
    public Result<Void> lift(@PathVariable Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        if (user.getIsDisabled() != 1) {
            throw new BusinessException("该用户当前不在禁用期");
        }
        creditService.liftDisable(userId, UserContext.currentUserId());
        return Result.success();
    }

    /** 信用分调整（留痕 + 站内通知） */
    @OperationLog(module = "信用分管理", content = "调整用户信用分")
    @PutMapping("/credit/{userId}")
    public Result<Void> adjustCredit(@PathVariable Long userId, @Valid @RequestBody CreditAdjustDTO dto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        int from = user.getCreditScore();
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getUserId, userId)
                .set(SysUser::getCreditScore, dto.getScore()));
        sendMessage(userId, "信用分调整通知",
                "管理员已将您的信用分由 " + from + " 分调整为 " + dto.getScore() + " 分，原因："
                        + dto.getReason() + "。请遵守预约规则，保持良好记录。");
        return Result.success();
    }

    private void sendMessage(Long userId, String title, String content) {
        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType("系统");
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }
}
