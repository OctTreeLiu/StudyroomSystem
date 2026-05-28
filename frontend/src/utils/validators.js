export function validatePhone11(rule, value, callback) {
  const v = value === null || value === undefined ? '' : String(value).trim()
  if (!v) return callback()
  if (/^\d{11}$/.test(v)) return callback()
  return callback(new Error('联系电话必须为11位纯数字'))
}

