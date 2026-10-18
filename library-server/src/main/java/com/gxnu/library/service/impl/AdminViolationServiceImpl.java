package com.gxnu.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnu.library.common.constant.ConfigKey;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.ResultCode;
import com.gxnu.library.dto.ViolationAddDTO;
import com.gxnu.library.entity.DisableRecord;
import com.gxnu.library.entity.SysMessage;
import com.gxnu.library.entity.SysUser;
import com.gxnu.library.entity.ViolationRecord;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.DisableRecordMapper;
import com.gxnu.library.mapper.SysMessageMapper;
import com.gxnu.library.mapper.SysUserMapper;
import com.gxnu.library.mapper.ViolationRecordMapper;
import com.gxnu.library.service.AdminViolationService;
import com.gxnu.library.service.ConfigService;
import com.gxnu.library.service.CreditService;
import com.gxnu.library.vo.AdminViolationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理端违规管理实现：手动添加复用 CreditService.deduct 事务链，撤销做反向恢复
 */
@Service
@RequiredArgsConstructor
public class AdminViolationServiceImpl implements AdminViolationService {

    private final ViolationRecordMapper violationMapper;
    private final SysUserMapper userMapper;
    private final DisableRecordMapper disableMapper;
    private final SysMessageMapper messageMapper;
    private final CreditService creditService;
    private final ConfigService configService;

    @Override
    public PageResult<AdminViolationVO> page(Long userId, String violationType,
                                             long pageNum, long pageSize) {
        Page<ViolationRecord> page = violationMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ViolationRecord>()
                        .eq(userId != null, ViolationRecord::getUserId, userId)
                        .eq(StringUtils.hasText(violationType), ViolationRecord::getViolationType, violationType)
                        .orderByDesc(ViolationRecord::getCreateTime));

        Map<Long, SysUser> userMap = batchUsers(page.getRecords()
                .stream().map(ViolationRecord::getUserId).distinct().toList());
        List<AdminViolationVO> list = page.getRecords().stream().map(record -> {
            AdminViolationVO vo = new AdminViolationVO();
            vo.setViolationId(record.getViolationId());
            vo.setUserId(record.getUserId());
            vo.setOrderId(record.getOrderId());
            vo.setViolationType(record.getViolationType());
            vo.setViolationDesc(record.getViolationDesc());
            vo.setDeductScore(record.getDeductScore());
            vo.setCreateBy(record.getCreateBy());
            vo.setCreateTime(record.getCreateTime());
            SysUser user = userMap.get(record.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setRealName(user.getName());
            }
            return vo;
        }).toList();

        PageResult<AdminViolationVO> result = new PageResult<>();
        result.setList(list);
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        return result;
    }

    @Override
    public void add(ViolationAddDTO dto, Long adminId) {
        SysUser user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        // deduct 事务链：扣分 → 违规记录 → 站内通知 → 低于阈值自动禁用
        creditService.deduct(dto.getUserId(), dto.getOrderId(), dto.getViolationType(),
                dto.getViolationDesc(), dto.getDeductScore(), adminId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long violationId, Long adminId) {
        ViolationRecord record = violationMapper.selectById(violationId);
        if (record == null) {
            throw new BusinessException("违规记录不存在");
        }
        // 恢复信用分（加回扣减分）
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getUserId, record.getUserId())
                .setSql("credit_score = credit_score + " + record.getDeductScore()));
        violationMapper.deleteById(violationId);

        SysUser user = userMapper.selectById(record.getUserId());
        int newScore = user.getCreditScore();

        // 联动解禁：分数恢复到阈值以上且当前处于禁用期 → 解除禁用
        if (user.getIsDisabled() == 1
                && newScore >= configService.getInt(ConfigKey.CREDIT_DISABLE_THRESHOLD, 60)) {
            disableMapper.update(null, new LambdaUpdateWrapper<DisableRecord>()
                    .eq(DisableRecord::getUserId, user.getUserId())
                    .eq(DisableRecord::getIsLifted, 0)
                    .set(DisableRecord::getIsLifted, 1)
                    .set(DisableRecord::getLiftBy, adminId)
                    .set(DisableRecord::getLiftTime, LocalDateTime.now()));
            userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getUserId, user.getUserId())
                    .set(SysUser::getIsDisabled, 0)
                    .set(SysUser::getDisableEndTime, null));
            sendMessage(user.getUserId(), "禁用解除通知",
                    "管理员撤销了您的违规记录，信用分已恢复至 " + newScore + " 分，预约功能已解除禁用。");
        }

        sendMessage(user.getUserId(), "违规撤销通知",
                "管理员撤销了您的违规记录（" + record.getViolationType() + "），信用分已恢复 "
                        + record.getDeductScore() + " 分，当前信用分 " + newScore + " 分。");
    }

    // ===== 私有方法 =====

    private Map<Long, SysUser> batchUsers(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
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
