// 常用表单校验规则

// 学号：6-20 位数字
export function isValidStudentNo(val) {
  return /^\d{6,20}$/.test(val)
}

// 手机号：1 开头 11 位
export function isValidPhone(val) {
  return /^1\d{10}$/.test(val)
}
