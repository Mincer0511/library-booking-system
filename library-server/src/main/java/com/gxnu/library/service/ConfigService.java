package com.gxnu.library.service;

/**
 * 系统配置 Service
 */
public interface ConfigService {

    /**
     * 获取 int 型配置，无配置返回默认值
     */
    int getInt(String configKey, int defaultValue);
}
