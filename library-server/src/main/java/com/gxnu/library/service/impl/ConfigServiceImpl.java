package com.gxnu.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxnu.library.entity.SysConfig;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.SysConfigMapper;
import com.gxnu.library.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统配置 Service 实现（带本地缓存）
 */
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final SysConfigMapper configMapper;

    /** 配置缓存（系统设置修改时会清除，Sprint 3 实现） */
    private final Map<String, Integer> cache = new ConcurrentHashMap<>();

    @Override
    public int getInt(String configKey, int defaultValue) {
        return cache.computeIfAbsent(configKey, key -> {
            SysConfig config = configMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
            if (config == null) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(config.getConfigValue().trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        });
    }

    @Override
    public List<SysConfig> listAll() {
        return configMapper.selectList(
                new LambdaQueryWrapper<SysConfig>().orderByAsc(SysConfig::getConfigId));
    }

    @Override
    public void updateValue(String configKey, String configValue) {
        SysConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, configKey));
        if (config == null) {
            throw new BusinessException("配置项不存在");
        }
        if (!StringUtils.hasText(configValue)) {
            throw new BusinessException("配置值不能为空");
        }
        config.setConfigValue(configValue.trim());
        configMapper.updateById(config);
        // 清除缓存，下次读取取新值，规则实时生效
        cache.remove(configKey);
    }
}
