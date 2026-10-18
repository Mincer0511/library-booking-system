import axios from 'axios'
import request from '@/api/request'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

// 管理端：数据统计（对应 AdminStatController）

// 数据看板聚合
export function getDashboard() {
  return request.get('/admin/stat/dashboard')
}

// 每日订单趋势（近 days 天）
export function getDailyCount(days = 14) {
  return request.get('/admin/stat/daily', { params: { days } })
}

// 热门开始时段（近 days 天）
export function getHotHours(days = 30) {
  return request.get('/admin/stat/hot-hours', { params: { days } })
}

// 自习室利用率（startDate / endDate 必填，floorId / roomId 可选）
export function getUtilization(params) {
  return request.get('/admin/stat/utilization', { params })
}

/**
 * 导出利用率报表 Excel（xlsx）
 * 说明：报表为文件流，不走统一 {code,msg,data} 拦截器，
 * 用原生 axios 以 blob 方式请求并手动带上 Authorization 头。
 */
export async function exportUtilization(startDate, endDate) {
  const defaultName = `自习室利用率报表_${startDate}_${endDate}.xlsx`
  const token = getToken()
  const res = await axios.get('/api/admin/stat/export', {
    params: { startDate, endDate },
    responseType: 'blob',
    timeout: 60000,
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })

  // 后端业务异常时返回的是 JSON（blob 包装），解析并提示
  if (res.data && res.data.type && res.data.type.includes('json')) {
    const text = await res.data.text()
    const result = JSON.parse(text)
    ElMessage.error(result.msg || '导出失败')
    throw new Error(result.msg || '导出失败')
  }

  // 从 Content-Disposition 解析文件名（后端 URLEncoder 编码），失败则用默认名
  const disposition = res.headers['content-disposition'] || ''
  let fileName = defaultName
  const match = disposition.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i)
  if (match && match[1]) {
    try {
      fileName = decodeURIComponent(match[1].replace(/\+/g, ' '))
    } catch (e) {
      fileName = defaultName
    }
  }

  // 触发浏览器下载
  const url = URL.createObjectURL(new Blob([res.data]))
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
