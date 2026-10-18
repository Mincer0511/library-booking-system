package com.gxnu.library.service;

import com.gxnu.library.entity.SysConfig;

import java.util.List;

/**
 * 系统配置 Service
 */
public interface ConfigService {

    /**
     * 获取 int 型配置，无配置返回默认值
     */
    int getInt(String configKey, int defaultValue);

    /**
     * 全部配置列表（管理端系统设置页）
     */
    List<SysConfig> listAll();

    /**
     * 更新配置值并清除本地缓存（预约/信用规则实时生效）
     */
    void updateValue(String configKey, String configValue);
}
