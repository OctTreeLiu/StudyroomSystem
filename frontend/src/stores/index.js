import { defineStore } from 'pinia'

const TOKEN_KEY = 'token'
const LAST_TAB_KEY = 'last_tab_id'

// 生成或获取当前标签的唯一 ID，使用 window.name 可在跨站跳转后保持
const ensureTabId = () => {
  if (window.name) return window.name
  // 如果没有 window.name，尝试使用最后一次记录的 tabId（支付回跳可能会丢失）
  const remembered = localStorage.getItem(LAST_TAB_KEY)
  if (remembered) {
    window.name = remembered
    return remembered
  }
  const tabId = crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`
  window.name = tabId
  localStorage.setItem(LAST_TAB_KEY, tabId)
  return tabId
}

// 尝试从本地存储中恢复当前标签的 token，即使 window.name 丢失也能找回
const resolveTokenFromStorage = () => {
  const tabId = ensureTabId()
  const namespacedKey = `${TOKEN_KEY}:${tabId}`

  // 优先当前 tabId 的 token
  const sessionToken = sessionStorage.getItem(namespacedKey)
  if (sessionToken) return sessionToken

  const tabToken = localStorage.getItem(namespacedKey)
  if (tabToken) {
    sessionStorage.setItem(namespacedKey, tabToken)
    return tabToken
  }

  // 兜底：如果存在全局 token（不带 tabId），先恢复后再回写到当前 tabId
  const globalToken = localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY)
  if (globalToken) {
    sessionStorage.setItem(namespacedKey, globalToken)
    localStorage.setItem(namespacedKey, globalToken)
    localStorage.setItem(LAST_TAB_KEY, tabId)
    return globalToken
  }

  // 如果 window.name 被清空且存在唯一的 namespaced token，则自动恢复并绑定
  const namespacedKeys = Object.keys(localStorage).filter(k => k.startsWith(`${TOKEN_KEY}:`))
  if (!window.name && namespacedKeys.length === 1) {
    const onlyKey = namespacedKeys[0]
    const recoveredTabId = onlyKey.split(':')[1]
    const recovered = localStorage.getItem(onlyKey)
    if (recovered) {
      window.name = recoveredTabId
      localStorage.setItem(LAST_TAB_KEY, recoveredTabId)
      sessionStorage.setItem(onlyKey, recovered)
      return recovered
    }
  }

  // 兼容旧的非命名空间 token
  const legacyToken = localStorage.getItem(TOKEN_KEY)
  if (legacyToken) {
    sessionStorage.setItem(namespacedKey, legacyToken)
    localStorage.removeItem(TOKEN_KEY)
    return legacyToken
  }

  return ''
}

const persistToken = (token) => {
  const tabId = ensureTabId()
  const namespacedKey = `${TOKEN_KEY}:${tabId}`
  sessionStorage.setItem(namespacedKey, token)
  localStorage.setItem(namespacedKey, token)
  localStorage.setItem(LAST_TAB_KEY, tabId)
  // 额外写入全局 key，便于回跳后兜底恢复
  sessionStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(TOKEN_KEY, token)
}

const clearToken = () => {
  const tabId = ensureTabId()
  const namespacedKey = `${TOKEN_KEY}:${tabId}`
  sessionStorage.removeItem(namespacedKey)
  localStorage.removeItem(namespacedKey)
  // 不删除 LAST_TAB_KEY，避免支付回跳时 window.name 丢失无法恢复
  // 清理遗留的非命名空间 token
  localStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(TOKEN_KEY)
}

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: resolveTokenFromStorage()
  }),
  
  getters: {
    isLogin: (state) => !!state.token,
    isAdmin: (state) => state.userInfo?.role === 1
  },
  
  actions: {
    setUserInfo(userInfo) {
      this.userInfo = userInfo
    },
    
    setToken(token) {
      this.token = token
      persistToken(token)
    },
    
    logout() {
      this.userInfo = null
      this.token = ''
      clearToken()
    }
  }
})

