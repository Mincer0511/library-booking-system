package com.gxnu.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gxnu.library.common.constant.ConfigKey;
import com.gxnu.library.entity.DisableRecord;
import com.gxnu.library.entity.SysMessage;
import com.gxnu.library.entity.SysUser;
import com.gxnu.library.entity.ViolationRecord;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.DisableRecordMapper;
import com.gxnu.library.mapper.SysMessageMapper;
import com.gxnu.library.mapper.SysUserMapper;
import com.gxnu.library.mapper.ViolationRecordMapper;
import com.gxnu.library.service.ConfigService;
import com.gxnu.library.service.CreditService;
import com.gxnu.library.utils.UserContext;
import com.gxnu.library.vo.CreditVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 信用分 Service 实现：扣分、违规记录、禁用判定、解禁均在同一事务内完成
 */
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final SysUserMapper userMapper;
    private final ViolationRecordMapper violationMapper;
    private final DisableRecordMapper disableMapper;
    private final SysMessageMapper messageMapper;
    private final ConfigService configService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(Long userId, Long orderId, String violationType, String desc, int deductScore, Long createBy) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 1. 扣减信用分（下限 0 分）
        int newScore = Math.max(0, user.getCreditScore() - deductScore);
        user.setCreditScore(newScore);
        userMapper.updateById(user);

        // 2. 生成违规记录
        ViolationRecord record = new ViolationRecord();
        record.setUserId(userId);
        record.setOrderId(orderId);
        record.setViolationType(violationType);
        record.setViolationDesc(desc);
        record.setDeductScore(deductScore);
        record.setCreateBy(createBy);
        violationMapper.insert(record);

        // 3. 站内违规通知
        sendMessage(userId, "违规通知",
                "您因「" + desc + "」被扣除信用分 " + deductScore + " 分，当前信用分 " + newScore + " 分。",
                "违规");

        // 4. 禁用判定：低于阈值且当前未禁用 → 禁用 + 禁用记录 + 通知
        int threshold = configService.getInt(ConfigKey.CREDIT_DISABLE_THRESHOLD, 60);
        if (newScore < threshold && user.getIsDisabled() == 0) {
            int disableDays = configService.getInt(ConfigKey.CREDIT_DISABLE_DAYS, 7);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = now.plusDays(disableDays);

            user.setIsDisabled(1);
            user.setDisableEndTime(endTime);
            userMapper.updateById(user);

            DisableRecord disable = new DisableRecord();
            disable.setUserId(userId);
            disable.setDisableReason("信用分低于" + threshold + "分（当前" + newScore + "分），系统自动禁用预约");
            disable.setStartTime(now);
            disable.setEndTime(endTime);
            disable.setIsLifted(0);
            disableMapper.insert(disable);

            sendMessage(userId, "预约禁用通知",
                    "您的信用分已低于 " + threshold + " 分，预约功能已被禁用 " + disableDays
                            + " 天，解禁时间：" + endTime.format(DT_FMT)
                            + "。解禁后信用分将恢复至 " + configService.getInt(ConfigKey.CREDIT_RESTORE_SCORE, 60) + " 分。",
                    "违规");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void liftDisable(Long userId, Long liftBy) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getIsDisabled() == 0) {
            return;
        }
        int restoreScore = configService.getInt(ConfigKey.CREDIT_RESTORE_SCORE, 60);

        // updateById 会忽略 null 字段，禁用到期时间需用 UpdateWrapper 显式置空
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getUserId, userId)
                .set(SysUser::getIsDisabled, 0)
                .set(SysUser::getDisableEndTime, null)
                .set(SysUser::getCreditScore, restoreScore));

        // 更新生效中的禁用记录
        DisableRecord record = disableMapper.selectOne(new LambdaQueryWrapper<DisableRecord>()
                .eq(DisableRecord::getUserId, userId)
                .eq(DisableRecord::getIsLifted, 0)
                .orderByDesc(DisableRecord::getCreateTime)
                .last("LIMIT 1"));
        if (record != null) {
            record.setIsLifted(1);
            record.setLiftBy(liftBy);
            record.setLiftTime(LocalDateTime.now());
            disableMapper.updateById(record);
        }

        sendMessage(userId, "解禁通知",
                "您的预约禁用已解除，信用分已恢复至 " + restoreScore + " 分，请遵守预约规则，文明用座。",
                "系统");
    }

    @Override
    public CreditVO myPanel() {
        Long userId = UserContext.currentUserId();
        SysUser user = userMapper.selectById(userId);

        List<ViolationRecord> records = violationMapper.selectList(new LambdaQueryWrapper<ViolationRecord>()
                .eq(ViolationRecord::getUserId, userId)
                .orderByDesc(ViolationRecord::getCreateTime)
                .last("LIMIT 10"));

        CreditVO vo = new CreditVO();
        vo.setCreditScore(user.getCreditScore());
        vo.setIsDisabled(user.getIsDisabled());
        vo.setDisableEndTime(user.getDisableEndTime());
        vo.setDisableThreshold(configService.getInt(ConfigKey.CREDIT_DISABLE_THRESHOLD, 60));
        vo.setRecentViolations(records);
        return vo;
    }

    @Override
    public List<ViolationRecord> myViolations() {
        Long userId = UserContext.currentUserId();
        return violationMapper.selectList(new LambdaQueryWrapper<ViolationRecord>()
                .eq(ViolationRecord::getUserId, userId)
                .orderByDesc(ViolationRecord::getCreateTime));
    }

    /** 发送站内消息 */
    private void sendMessage(Long userId, String title, String content, String type) {
        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setIsRead(0);
        messageMapper.insert(message);
    }
}
