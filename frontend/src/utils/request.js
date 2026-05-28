import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores'

// 创建axios实例
const service = axios.create({
  baseURL: '/api',
  timeout: 30000 // 增加超时时间到30秒
})

// 获取当前标签可用的 token，尽量在 window.name 丢失时也能恢复
const getToken = () => {
  const tabId = window.name || localStorage.getItem('last_tab_id') || ''
  const namespacedKey = tabId ? `token:${tabId}` : 'token'

  const sessionToken = sessionStorage.getItem(namespacedKey)
  if (sessionToken) return sessionToken

  const localToken = localStorage.getItem(namespacedKey)
  if (localToken) return localToken

  // 当 window.name 丢失但只有一个 namespaced token 时，尝试恢复
  const namespacedKeys = Object.keys(localStorage).filter(k => k.startsWith('token:'))
  if (!tabId && namespacedKeys.length === 1) {
    const onlyKey = namespacedKeys[0]
    const recovered = localStorage.getItem(onlyKey)
    if (recovered) {
      const recoveredTabId = onlyKey.split(':')[1]
      window.name = recoveredTabId
      return recovered
    }
  }

  return localStorage.getItem('token')
}

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 如果返回的状态码不是200，则认为是错误
    if (res.code !== 200) {
      const message = res.message || '请求失败'
      // 检查是否是token相关的错误
      if (message.includes('无效的token') || message.includes('token过期') || message.includes('未授权')) {
        handleTokenExpired()
        return Promise.reject(new Error('登录已过期，请重新登录'))
      }
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    // 处理网络错误（无响应）
    if (!error.response) {
      // 检查是否是超时错误
      if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
        ElMessage.error('请求超时，请检查网络连接或稍后重试')
        return Promise.reject(new Error('请求超时'))
      }
      // 其他网络错误
      ElMessage.error('网络连接失败，请检查网络连接')
      return Promise.reject(error)
    }
    
    // 处理HTTP状态码
    const status = error.response.status
    const res = error.response.data
    const message = res?.message || error.message
    
      // 403 禁止访问（权限不足）
      if (status === 403) {
        const errorMsg = res?.message || '无权限访问'
        // 如果是token无效或过期导致的403，需要重新登录
        if (errorMsg.includes('无效的token') || errorMsg.includes('token过期') || errorMsg.includes('未授权')) {
          handleTokenExpired()
          return Promise.reject(new Error('登录已过期，请重新登录'))
        }
        ElMessage.error(errorMsg)
        return Promise.reject(new Error(errorMsg))
      }
      
      // 401 未授权
      if (status === 401) {
        handleTokenExpired()
        return Promise.reject(new Error('登录已过期，请重新登录'))
      }
      
      // 500 服务器错误
      if (status === 500) {
        ElMessage.error('服务器错误，请稍后重试')
        return Promise.reject(new Error('服务器错误'))
      }
      
      // 检查响应数据中的错误消息
      if (res && res.code !== 200) {
        const errorMsg = res.message || '请求失败'
        // 检查是否是token相关的错误
        if (errorMsg.includes('无效的token') || errorMsg.includes('token过期') || errorMsg.includes('未授权')) {
          handleTokenExpired()
          return Promise.reject(new Error('登录已过期，请重新登录'))
        }
        ElMessage.error(errorMsg)
        return Promise.reject(new Error(errorMsg))
      }
    
    // 其他HTTP错误
    if (status >= 400 && status < 500) {
      ElMessage.error(message || '请求失败，请检查输入')
    } else {
      ElMessage.error(message || '服务器错误，请稍后重试')
    }
    
    return Promise.reject(error)
  }
)

// 处理token过期的函数（防止重复调用）
let isHandlingTokenExpired = false

function handleTokenExpired() {
  // 防止重复处理
  if (isHandlingTokenExpired) {
    return
  }
  
  isHandlingTokenExpired = true
  
  // 清除token和用户信息
  const userStore = useUserStore()
  userStore.logout()
  
  // 显示提示信息（只显示一次）
  ElMessage.warning('登录已过期，请重新登录')
  
  // 跳转到登录页
  if (router.currentRoute.value.path !== '/login') {
    router.push({
      path: '/login',
      query: { redirect: router.currentRoute.value.fullPath }
    }).finally(() => {
      // 延迟重置标志，避免立即重复
      setTimeout(() => {
        isHandlingTokenExpired = false
      }, 1000)
    })
  } else {
    // 如果已经在登录页，直接重置标志
    setTimeout(() => {
      isHandlingTokenExpired = false
    }, 1000)
  }
}

export default service

