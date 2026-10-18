import request from '@/api/request'

// 管理端：系统设置（对应 AdminConfigController，超管专属）

// 全部配置列表（预约与信用规则参数）
export function getConfigList() {
  return request.get('/admin/config/list')
}

// 修改配置值（configValue 必须为非负整数字符串，修改后实时生效）
export function updateConfig(configKey, configValue) {
  return request.put('/admin/config', { configKey, configValue: String(configValue) })
}
