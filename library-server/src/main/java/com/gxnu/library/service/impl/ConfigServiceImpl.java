package com.gxnu.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxnu.library.entity.SysConfig;
import com.gxnu.library.mapper.SysConfigMapper;
import com.gxnu.library.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
